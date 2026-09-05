/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.Util
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.LoadingOverlay
 *  net.minecraft.client.gui.screens.Overlay
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.resources.ClientPackSource
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  org.apache.logging.log4j.LogManager
 *  org.apache.logging.log4j.Logger
 */
package com.netease.mc.mod.departmod;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DepartWrapper {
    private static final Logger LOGGER = LogManager.getLogger();
    private static ResourceLocation mojangResource = new ResourceLocation("depart:textures/gui/mojang_new.png");
    private static ResourceLocation urlResource = new ResourceLocation("depart:textures/gui/url.png");
    private static Map<String, Component> SPECIAL_PACK_NAMES = Map.of("programmer_art", Component.m_237115_((String)"\u7ecf\u5178"), "high_contrast", Component.m_237115_((String)"\u9ad8\u5bf9\u6bd4\u5ea6"));
    private static final DecimalFormat decimalFormat = new DecimalFormat("########0.00");

    public static Component getPackTitleWrapper(ClientPackSource source, String text) {
        Component $$1 = SPECIAL_PACK_NAMES.get(text);
        return $$1 != null ? $$1 : Component.m_237113_((String)text);
    }

    private static void drawMojang(GuiGraphics guiGraphics) {
        int width = Minecraft.m_91087_().m_91268_().m_85445_();
        int height = Minecraft.m_91087_().m_91268_().m_85446_();
        double guiScale = Minecraft.m_91087_().m_91268_().m_85449_();
        int mojangPicWidth = (int)(516.0 / guiScale);
        int mojangPicHeight = (int)(152.0 / guiScale);
        int x = (width - mojangPicWidth) / 2;
        int y = (height - mojangPicHeight) / 2;
        guiGraphics.m_280163_(mojangResource, x, y, 0.0f, 0.0f, mojangPicWidth, mojangPicHeight, mojangPicWidth, mojangPicHeight);
    }

    private static void drawUrl(GuiGraphics guiGraphics) {
        int urlPicWidth = Minecraft.m_91087_().m_91268_().m_85445_();
        int urlPicHeight = 58 * urlPicWidth / 1920;
        int x = 0;
        int y = Minecraft.m_91087_().m_91268_().m_85446_() - urlPicHeight;
        guiGraphics.m_280163_(urlResource, x, y, 0.0f, 0.0f, urlPicWidth, urlPicHeight, urlPicWidth, urlPicHeight);
    }

    public static void renderWrapper(LoadingOverlay loadingOverlay, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        float f2;
        float f1;
        Minecraft mc = Minecraft.m_91087_();
        int i = guiGraphics.m_280182_();
        int j = guiGraphics.m_280206_();
        long k = Util.m_137550_();
        if (loadingOverlay.f_96166_ && loadingOverlay.f_96169_ == -1L) {
            loadingOverlay.f_96169_ = k;
        }
        float f = loadingOverlay.f_96168_ > -1L ? (float)(k - loadingOverlay.f_96168_) / 1000.0f : -1.0f;
        float f3 = f1 = loadingOverlay.f_96169_ > -1L ? (float)(k - loadingOverlay.f_96169_) / 500.0f : -1.0f;
        if (f >= 1.0f) {
            if (loadingOverlay.f_96163_.f_91080_ != null) {
                loadingOverlay.f_96163_.f_91080_.m_88315_(guiGraphics, 0, 0, partialTicks);
            }
            int l = Mth.m_14167_((float)((1.0f - Mth.m_14036_((float)(f - 1.0f), (float)0.0f, (float)1.0f)) * 255.0f));
            guiGraphics.m_285944_(RenderType.m_286086_(), 0, 0, i, j, LoadingOverlay.m_169324_((int)LoadingOverlay.f_96161_.getAsInt(), (int)l));
            f2 = 1.0f - Mth.m_14036_((float)(f - 1.0f), (float)0.0f, (float)1.0f);
        } else if (loadingOverlay.f_96166_) {
            if (loadingOverlay.f_96163_.f_91080_ != null && f1 < 1.0f) {
                loadingOverlay.f_96163_.f_91080_.m_88315_(guiGraphics, mouseX, mouseY, partialTicks);
            }
            int l1 = Mth.m_14165_((double)(Mth.m_14008_((double)f1, (double)0.15, (double)1.0) * 255.0));
            guiGraphics.m_285944_(RenderType.m_286086_(), 0, 0, i, j, LoadingOverlay.m_169324_((int)LoadingOverlay.f_96161_.getAsInt(), (int)l1));
            f2 = Mth.m_14036_((float)f1, (float)0.0f, (float)1.0f);
        } else {
            int i2 = LoadingOverlay.f_96161_.getAsInt();
            GlStateManager._clearColor((float)255.0f, (float)255.0f, (float)255.0f, (float)1.0f);
            GlStateManager._clear((int)16384, (boolean)Minecraft.f_91002_);
            DepartWrapper.drawMojang(guiGraphics);
            DepartWrapper.drawUrl(guiGraphics);
            f2 = 1.0f;
        }
        int j2 = (int)((double)guiGraphics.m_280182_() * 0.5);
        int k2 = (int)((double)guiGraphics.m_280206_() * 0.5);
        double d1 = Math.min((double)guiGraphics.m_280182_() * 0.75, (double)guiGraphics.m_280206_()) * 0.25;
        int i1 = (int)(d1 * 0.5);
        double d0 = d1 * 4.0;
        int j1 = (int)(d0 * 0.5);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc((int)770, (int)1);
        guiGraphics.m_280246_(1.0f, 1.0f, 1.0f, f2);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        int k1 = (int)((double)guiGraphics.m_280206_() * 0.8325);
        float f6 = loadingOverlay.f_96164_.m_7750_();
        loadingOverlay.f_96167_ = Mth.m_14036_((float)(loadingOverlay.f_96167_ * 0.95f + f6 * 0.050000012f), (float)0.0f, (float)1.0f);
        if (f < 1.0f) {
            loadingOverlay.m_96182_(guiGraphics, i / 2 - j1, k1 - 5, i / 2 + j1, k1 + 5, 1.0f - Mth.m_14036_((float)f, (float)0.0f, (float)1.0f));
        }
        if (f >= 2.0f) {
            loadingOverlay.f_96163_.m_91150_((Overlay)null);
        }
        if (loadingOverlay.f_96168_ == -1L && loadingOverlay.f_96164_.m_7746_() && (!loadingOverlay.f_96166_ || f1 >= 2.0f)) {
            loadingOverlay.f_96168_ = Util.m_137550_();
            try {
                loadingOverlay.f_96164_.m_7748_();
                loadingOverlay.f_96165_.accept(Optional.empty());
            }
            catch (Throwable var23) {
                Throwable throwable = var23;
                loadingOverlay.f_96165_.accept(Optional.of(throwable));
            }
            if (loadingOverlay.f_96163_.f_91080_ != null) {
                loadingOverlay.f_96163_.f_91080_.m_6575_(loadingOverlay.f_96163_, guiGraphics.m_280182_(), guiGraphics.m_280206_());
            }
        }
    }

    public static String timeWrapper(int number) {
        double d0 = (double)number / 20.0;
        double d1 = d0 / 60.0;
        double d2 = d1 / 60.0;
        double d3 = d2 / 24.0;
        double d4 = d3 / 365.0;
        if (d4 > 0.5) {
            return decimalFormat.format(d4) + "\u5e74";
        }
        if (d3 > 0.5) {
            return decimalFormat.format(d3) + "\u5929";
        }
        if (d2 > 0.5) {
            return decimalFormat.format(d2) + "\u5c0f\u65f6";
        }
        return d1 > 0.5 ? decimalFormat.format(d1) + "\u5206" : d0 + "\u79d2";
    }

    public static String distanceWrapper(int number) {
        double d0 = (double)number / 100.0;
        double d1 = d0 / 1000.0;
        if (d1 > 0.5) {
            return decimalFormat.format(d1) + "\u5343\u7c73";
        }
        return d0 > 0.5 ? decimalFormat.format(d0) + "\u7c73" : number + "\u5398\u7c73";
    }

    public static String getNameWrapper() {
        return "\u7ec4\u4ef6\u8d44\u6e90\u5305";
    }
}

