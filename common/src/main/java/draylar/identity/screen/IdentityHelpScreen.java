package draylar.identity.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class IdentityHelpScreen extends Screen {

    public IdentityHelpScreen() {
        super(new LiteralText(""));
        super.init(MinecraftClient.getInstance(), MinecraftClient.getInstance().getWindow().getScaledWidth(), MinecraftClient.getInstance().getWindow().getScaledHeight());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);

        matrices.push();
        matrices.scale(0.75f, 0.75f, 0.75f);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.welcome"), 15, 15, 0xffffff);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.credits"), 15, 30, 0xffffff);

        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.support_label").formatted(Formatting.BOLD), 15, 60, 0xffffff);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.support_description"), 15, 75, 0xffffff);

        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.ability_label").formatted(Formatting.BOLD), 15, 100, 0xffffff);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.ability_description_1"), 15, 115, 0xffffff);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.ability_description_2"), 15, 130, 0xffffff);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.ability_description_3"), 15, 145, 0xffffff);

        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.config_label").formatted(Formatting.BOLD), 15, 175, 0xffffff);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.config_description"), 15, 190, 0xffffff);

        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.credits_label").formatted(Formatting.BOLD), 15, 220, 0xffffff);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.credits_general"), 15, 235, 0xffffff);
        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.credits_translators"), 15, 250, 0xffffff);

        MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, new TranslatableText("identity.help.return").formatted(Formatting.ITALIC), 15, height + 60, 0xffffff);

        matrices.pop();

        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        MinecraftClient.getInstance().setScreen(new IdentityScreen());
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
