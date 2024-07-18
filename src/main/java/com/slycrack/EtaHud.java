package com.slycrack;

import org.rusherhack.client.api.feature.hud.TextHudElement;
import org.rusherhack.client.api.render.RenderContext;
import org.rusherhack.client.api.render.font.IFontRenderer;

public class EtaHud extends TextHudElement {

    private final EtaCommand etaCommand;

    public EtaHud(EtaCommand etaCommand) {
        super("EtaHud");
        this.etaCommand = etaCommand;
    }

    @Override
    public String getText() {
        return etaCommand.calculateETA();
    }

    @Override
    public double getWidth() {
        return getFontRenderer().getStringWidth(getText()) + PADDING * 2;
    }

    @Override
    public double getHeight() {
        return getFontRenderer().getFontHeight() + PADDING * 2;
    }

    @Override
    public void renderContent(RenderContext context, double mouseX, double mouseY) {
        IFontRenderer fontRenderer = getFontRenderer();
        String text = getText();
        double x = PADDING;
        double y = PADDING;
        fontRenderer.drawString(text, x, y, 0xFFFFFFFF, true);
    }
}
