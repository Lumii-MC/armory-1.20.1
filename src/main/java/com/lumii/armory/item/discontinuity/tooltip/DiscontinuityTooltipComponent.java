package com.lumii.armory.item.discontinuity.tooltip;

import com.lumii.armory.item.discontinuity.DiscontinuityItem;
import com.lumii.armory.item.discontinuity.PartVariant;
import com.lumii.armory.item.discontinuity.VariantData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

public class DiscontinuityTooltipComponent implements TooltipComponent {
    private static final float SCALE = 3f;
    private static final float SPACING = 12f;
    private static final int LEFT_PAD = 120;
    private static final float LINE_Y = 34f;
    private static final float SQRT2 = 1.41421356237f;

    private final ItemStack stack;

    public DiscontinuityTooltipComponent(ItemStack stack) {
        this.stack = stack;
    }

    private PartVariant[] parts() {
        VariantData data = DiscontinuityItem.getVariantData(stack);
        return new PartVariant[] { data.handle(), data.mid(), data.end() };
    }

    private static float[] shifts(PartVariant[] parts) {
        float[] out = new float[parts.length];
        float accumulated = 0f;
        float full = 0f;
        for (int i = 0; i < parts.length; i++) {
            full += parts[i].getFullOffset();
            out[i] = accumulated + full - 1f;
            accumulated += parts[i].getOffsetNext();
        }
        return out;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getWidth(TextRenderer textRenderer) {
        PartVariant[] parts = parts();
        float[] shifts = shifts(parts);
        float right = 0f;
        for (int i = 0; i < parts.length; i++) {
            float size = 16 * parts[i].getScaleFactor();
            right = Math.max(right, i * SPACING + (shifts[i] + size) * SQRT2 * 2f);
        }
        return LEFT_PAD + (int) Math.ceil(right);
    }

    @Override
    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        MatrixStack matrices = context.getMatrices();
        PartVariant[] parts = parts();
        float[] shifts = shifts(parts);

        for (int i = 0; i < parts.length; i++) {
            PartVariant part = parts[i];
            int size = 16 * part.getScaleFactor();

            matrices.push();
            matrices.translate(x + LEFT_PAD + i * SPACING, y + LINE_Y, 0);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45));
            matrices.scale(SCALE, SCALE, 1);
            matrices.translate(shifts[i], -shifts[i], 0);
            context.drawTexture(part.getTextureId(), 0, -size, 0, 0, size, size, size, size);
            matrices.pop();
        }
    }
}