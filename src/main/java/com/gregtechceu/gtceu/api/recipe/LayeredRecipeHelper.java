package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.data.recipe.builder.LayeredRecipeInfo;

import com.lowdragmc.lowdraglib.syncdata.payload.ObjectTypedPayload;

import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.Streams;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LayeredRecipeHelper {

    public static boolean hasLayeredSteps(GTRecipe recipe) {
        return recipe.data.contains("layered_steps");
    }

    public static List<Layer> getLayeredSteps(GTRecipe recipe) {
        var serialized = recipe.data.get("layered_steps");
        return Layer.CODEC.listOf().parse(NbtOps.INSTANCE, serialized).result().orElseThrow();
    }

    public static void setLayeredSteps(GTRecipe recipe, List<Layer> layers) {
        var serialized = Layer.CODEC.listOf().encodeStart(NbtOps.INSTANCE, layers).result().orElseThrow();
        recipe.data.put("layered_steps", serialized);
    }

    public static @Nullable List<Layer> calculateRecipeSteps(GTRecipe recipe) {
        var layeredInfo = parseRecipeInfo(recipe);
        if (layeredInfo == null) return null;
        var base = createBaseRecipe(recipe, layeredInfo);
        return IntStream.range(0, layeredInfo.layers().size())
                .mapToObj((index) -> createStepRecipe(recipe, base, layeredInfo, index))
                .toList();
    }

    public static BiConsumer<GTRecipeBuilder, Consumer<FinishedRecipe>> onSaveLayeredRecipe(Supplier<GTRecipeType> syntheticType) {
        return (builder, consumer) -> {
            if (!builder.data.contains("layered_info")) return;

            var built = builder.buildRawRecipe();
            var layers = calculateRecipeSteps(built);
            assert layers != null;

            syntheticType.get().copyFrom(builder).id(builder.id).save(consumer);

            resetRecipeBuilder(builder, layers.get(0).recipe, builder.recipeType);
            var serializedSteps = Layer.CODEC.listOf().encodeStart(NbtOps.INSTANCE, layers).result().orElseThrow();
            builder.data.remove("is_layer");
            builder.data.put("layered_steps", serializedSteps);
        };
    }

    private static void resetRecipeBuilder(GTRecipeBuilder builder, ResourceLocation id, GTRecipeType recipeType) {
        builder.input.clear();
        builder.tickInput.clear();
        builder.output.clear();
        builder.tickOutput.clear();
        builder.inputChanceLogic.clear();
        builder.outputChanceLogic.clear();
        builder.tickInputChanceLogic.clear();
        builder.tickOutputChanceLogic.clear();
        builder.conditions.clear();
        builder.data = new CompoundTag();
        builder.id = id;
        builder.recipeType = recipeType;
        builder.recipeCategory = recipeType.getCategory();
        builder.duration = 100;
        builder.perTick = false;
        builder.chance = ChanceLogic.getMaxChancedValue();
        builder.maxChance = ChanceLogic.getMaxChancedValue();
        builder.tierChanceBoost = 0;
        builder.addMaterialInfo(false, false);
        builder.onSave = null;
        builder.researchRecipeEntries().clear();
        builder.setTempItemStacks(new ArrayList<>());
        builder.setTempItemMaterialStacks(new ArrayList<>());
        builder.setTempFluidMaterialStacks(new ArrayList<>());
    }

    private static void resetRecipeBuilder(GTRecipeBuilder builder, GTRecipe toCopy, GTRecipeType recipeType) {
        resetRecipeBuilder(builder, toCopy.id, recipeType);
        toCopy.inputs.forEach((k, v) -> builder.input.put(k, new ArrayList<>(v)));
        toCopy.outputs.forEach((k, v) -> builder.output.put(k, new ArrayList<>(v)));
        toCopy.tickInputs.forEach((k, v) -> builder.tickInput.put(k, new ArrayList<>(v)));
        toCopy.tickOutputs.forEach((k, v) -> builder.tickOutput.put(k, new ArrayList<>(v)));
        builder.inputChanceLogic.putAll(toCopy.inputChanceLogics);
        builder.outputChanceLogic.putAll(toCopy.outputChanceLogics);
        builder.tickInputChanceLogic.putAll(toCopy.tickInputChanceLogics);
        builder.tickOutputChanceLogic.putAll(toCopy.tickOutputChanceLogics);
        builder.conditions.addAll(toCopy.conditions);
        builder.data = toCopy.data.copy();
        builder.duration = toCopy.duration;
        builder.recipeCategory = toCopy.recipeCategory;
    }

    private static Map<RecipeCapability<?>, List<Content>> copyLayeredInputs(Map<RecipeCapability<?>, List<Content>> inputMap,
                                                                             Map<RecipeCapability<?>, Int2IntMap> layeredInputMap,
                                                                             int recipeStep) {
        var dest = new IdentityHashMap<RecipeCapability<?>, List<Content>>();
        for (var entry : inputMap.entrySet()) {
            var capability = entry.getKey();
            var contents = entry.getValue();
            var layerInput = layeredInputMap.get(capability);
            if (layerInput == null) {
                if (recipeStep == -1) {
                    dest.put(capability, contents);
                }
                continue;
            }
            dest.put(capability, IntStream.range(0, contents.size()).boxed()
                    .flatMap(index -> layerInput.getOrDefault((int) index, -1) == recipeStep ?
                            Stream.of(contents.get(index)) : Stream.of())
                    .collect(Collectors.toList()));
        }
        return dest;
    }

    private static Layer createStepRecipe(GTRecipe fullRecipe, GTRecipe baseRecipe, LayeredRecipeInfo layeredInfo,
                                          int recipeStep) {
        var copy = baseRecipe.copy();
        copy.setId(copy.id.withSuffix("/step" + (recipeStep + 1)));
        for (var entry : copyLayeredInputs(fullRecipe.inputs, layeredInfo.input(), recipeStep).entrySet()) {
            copy.inputs.merge(entry.getKey(), entry.getValue(),
                    (contents1, contents2) -> Streams.concat(contents1.stream(), contents2.stream()).toList());
        }
        for (var entry : copyLayeredInputs(fullRecipe.tickInputs, layeredInfo.tickInput(), recipeStep).entrySet()) {
            copy.tickInputs.merge(entry.getKey(), entry.getValue(),
                    (contents1, contents2) -> Streams.concat(contents1.stream(), contents2.stream()).toList());
        }
        if (recipeStep == layeredInfo.layers().size() - 1) {
            copy.outputs.putAll(fullRecipe.outputs);
        }
        var layer = layeredInfo.layers().get(recipeStep);
        if (layer.duration() > 0) copy.duration = layer.duration();

        return new Layer(copy, layer.timeout());
    }

    private static GTRecipe createBaseRecipe(GTRecipe fullRecipe, LayeredRecipeInfo layeredInfo) {
        var copiedData = fullRecipe.data.copy();
        copiedData.remove("layered_info");
        copiedData.putBoolean("is_layer", true);

        return new GTRecipe(
                fullRecipe.recipeType, fullRecipe.id,
                copyLayeredInputs(fullRecipe.inputs, layeredInfo.input(), -1),
                new IdentityHashMap<>(),
                copyLayeredInputs(fullRecipe.tickInputs, layeredInfo.tickInput(), -1),
                new IdentityHashMap<>(),
                fullRecipe.inputChanceLogics,
                new IdentityHashMap<>(),
                fullRecipe.tickInputChanceLogics,
                new IdentityHashMap<>(),
                fullRecipe.conditions,
                fullRecipe.ingredientActions,
                copiedData,
                fullRecipe.duration,
                fullRecipe.recipeCategory);
    }

    private static @Nullable LayeredRecipeInfo parseRecipeInfo(GTRecipe recipe) {
        var layeredInfoTag = recipe.data.get("layered_info");
        return LayeredRecipeInfo.CODEC.parse(NbtOps.INSTANCE, layeredInfoTag).result().orElse(null);
    }

    public static class LayerPayload extends ObjectTypedPayload<Layer> {

        @Override
        public @Nullable Tag serializeNBT() {
            return Layer.CODEC.encodeStart(NbtOps.INSTANCE, payload).result().orElseThrow();
        }

        @Override
        public void deserializeNBT(Tag tag) {
            payload = Layer.CODEC.parse(NbtOps.INSTANCE, tag).result().orElseThrow();
        }
    }

    @Accessors(chain = true, fluent = true)
    public static class Layer {

        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(layer -> layer.recipe.id),
                GTRecipeSerializer.CODEC.fieldOf("recipe").forGetter(Layer::recipe),
                Codec.INT.fieldOf("timeout").forGetter(Layer::timeout)).apply(instance, Layer::new));

        @Getter
        private final GTRecipe recipe;

        @Getter
        private final int timeout;

        public Layer(ResourceLocation id, GTRecipe recipe, int timeout) {
            this.recipe = recipe.copy();
            this.recipe.id = id;
            this.timeout = timeout;
        }

        public Layer(GTRecipe recipe, int timeout) {
            this.recipe = recipe;
            this.timeout = timeout;
        }
    }
}
