package de.castcrafter.travel_anchors.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import de.castcrafter.travel_anchors.TravelAnchorList;
import de.castcrafter.travel_anchors.TravelAnchors;
import de.castcrafter.travel_anchors.blocks.TravelAnchorTile;
import de.castcrafter.travel_anchors.enchantments.TeleportationEnchantment;
import de.castcrafter.travel_anchors.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import static de.castcrafter.travel_anchors.TeleportHandler.isAnchor;

@Mod.EventBusSubscriber(modid = TravelAnchors.MODID)
public class SpecialRender extends TileEntityRenderer<TravelAnchorTile> {

    public static final ResourceLocation ANCHOR_MODEL = new ResourceLocation(TravelAnchors.MODID, "block/travel_anchor");
    private static IBakedModel ANCHOR_MODEL_BAKED = null;

    public static final ResourceLocation ANCHOR = new ResourceLocation(TravelAnchors.MODID, "textures/block/travel_anchor_port.png");
    private static boolean holdingItem = false;

    public SpecialRender(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(TravelAnchorTile tileEntity, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        if (tileEntity.getMimic() == null || tileEntity.getMimic().getBlock() == tileEntity.getBlockState().getBlock() || tileEntity.getMimic().isAir()) {
            IVertexBuilder vertexBuffer = buffer.getBuffer(RenderTypeLookup.func_239220_a_(tileEntity.getBlockState(), false));
            Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelRenderer()
                    .renderModelBrightnessColor(matrixStack.getLast(), vertexBuffer, tileEntity.getBlockState(),
                            ANCHOR_MODEL_BAKED, 1, 1, 1, combinedLight, combinedOverlay);
        } else {
            Minecraft.getInstance().getBlockRendererDispatcher().renderBlock(tileEntity.getMimic(), matrixStack, buffer, combinedLight, combinedOverlay);
        }

        PlayerEntity player = Minecraft.getInstance().player;
        if(holdingItem || isAnchor(player.world.getBlockState(player.getPosition().down())) || TeleportationEnchantment.hasTeleportation()){
            String text = "Test";
            renderText(text, matrixStack, buffer);
        }
    }

    public static void renderText(String text, MatrixStack matrixStack, IRenderTypeBuffer buffer) {
        float widthHalf = Minecraft.getInstance().fontRenderer.getStringWidth(text) / 2f;
        float heightHalf = Minecraft.getInstance().fontRenderer.FONT_HEIGHT / 2f;

        matrixStack.push();
        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;
        matrixStack.translate(0.5, 1.5, 0.5);
        matrixStack.scale(-0.1f, -0.1f, -0.1f);
        matrixStack.rotate(Minecraft.getInstance().getRenderManager().getCameraOrientation());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //noinspection deprecation
        GlStateManager.color4f(0.8f, 0.8f, 1f, 1);
        Minecraft.getInstance().getTextureManager().bindTexture(ANCHOR);
        AbstractGui.blit(matrixStack, -8, 2, 0, 0, 16, 16, 16, 16);

        //noinspection deprecation
        GlStateManager.color4f(1, 1, 1, 1);

        Minecraft.getInstance().fontRenderer.drawString(matrixStack, text, -widthHalf, -heightHalf, 0xFFFFFF);
        RenderSystem.disableBlend();
        matrixStack.pop();
    }

    @SubscribeEvent
    public static void checkItem(TickEvent.PlayerTickEvent event){
        PlayerEntity player = event.player;
        holdingItem = player.getHeldItem(Hand.MAIN_HAND).getItem() == Registration.TRAVEL_STAFF.get() || player.getHeldItem(Hand.OFF_HAND).getItem() == Registration.TRAVEL_STAFF.get();
    }
    public static void register(){
        ClientRegistry.bindTileEntityRenderer(Registration.TRAVEL_ANCHOR_TILE.get(), SpecialRender::new);
    }

    public static void bakeModels(final ModelBakeEvent event) {
        ANCHOR_MODEL_BAKED = event.getModelRegistry().get(ANCHOR_MODEL);
    }
}