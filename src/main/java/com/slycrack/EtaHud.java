package com.slycrack;

import org.rusherhack.client.api.feature.hud.TextHudElement;
import org.rusherhack.client.api.render.RenderContext;
import org.rusherhack.client.api.render.font.IFontRenderer;

public class EtaHud extends TextHudElement {

    private final EtaCommand etaCommand;
    private long lastUpdateTime = 0;
    private String cachedText = "";

    public EtaHud(EtaCommand etaCommand) {
        super("EtaHud");
        this.etaCommand = etaCommand;
    }

    @Override
    public String getText() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime > 1000) { // Update every second
            cachedText = etaCommand.calculateETA();
            lastUpdateTime = currentTime;
        }
        return cachedText;
    }

    @Override
    public double getWidth() {
        String text = getText();
        if (text.isEmpty()) {
            return 0;
        }
        return getFontRenderer().getStringWidth(text) + PADDING * 2;
    }

    @Override
    public double getHeight() {
        String text = getText();
        if (text.isEmpty()) {
            return 0;
        }
        return getFontRenderer().getFontHeight() + PADDING * 2;
    }

    @Override
    public void renderContent(RenderContext context, double mouseX, double mouseY) {
        String text = getText();
        if (!text.isEmpty()) {
            IFontRenderer fontRenderer = getFontRenderer();
            double x = PADDING;
            double y = PADDING;
            fontRenderer.drawString(text, x, y, 0xFFFFFFFF, true);
        }
    }
}
