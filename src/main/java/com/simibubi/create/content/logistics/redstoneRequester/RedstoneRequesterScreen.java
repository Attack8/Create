package com.simibubi.create.content.logistics.redstoneRequester;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class RedstoneRequesterScreen extends AbstractSimiContainerScreen<RedstoneRequesterMenu> {

	private EditBox addressBox;
	private IconButton confirmButton;
	private List<Rect2i> extraAreas = Collections.emptyList();
	private List<Integer> amounts = new ArrayList<>();

	private IconButton dontAllowPartial;
	private IconButton allowPartial;

	public RedstoneRequesterScreen(RedstoneRequesterMenu container, Inventory inv, Component title) {
		super(container, inv, title);

		for (int i = 0; i < 9; i++)
			amounts.add(1);

		List<BigItemStack> stacks = menu.contentHolder.encodedRequest.stacks();
		for (int i = 0; i < stacks.size(); i++)
			amounts.set(i, Math.max(1, stacks.get(i).count));
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		addressBox.tick();
		for (int i = 0; i < amounts.size(); i++)
			if (menu.ghostInventory.getStackInSlot(i)
				.isEmpty())
				amounts.set(i, 1);
	}

	@Override
	protected void init() {
		int bgHeight = AllGuiTextures.REDSTONE_REQUESTER.getHeight();
		int bgWidth = AllGuiTextures.REDSTONE_REQUESTER.getWidth();
		setWindowSize(bgWidth, bgHeight + AllGuiTextures.PLAYER_INVENTORY.getHeight());
		super.init();
		clearWidgets();
		int x = getGuiLeft();
		int y = getGuiTop();

		if (addressBox == null) {
			addressBox = new AddressEditBox(this, new NoShadowFontWrapper(font), x + 50, y + 53, 110, 10);
			addressBox.setValue(menu.contentHolder.encodedTargetAdress);
			addressBox.setTextColor(0x555555);
		}
		addRenderableWidget(addressBox);

		confirmButton = new IconButton(x + bgWidth - 33, y + bgHeight - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> minecraft.player.closeContainer());
		addRenderableWidget(confirmButton);

		allowPartial = new IconButton(x + 12, y + bgHeight - 24, AllIcons.I_PARTIAL_REQUESTS);
		allowPartial.withCallback(() -> {
			allowPartial.green = true;
			dontAllowPartial.green = false;
		});
		allowPartial.green = menu.contentHolder.allowPartialRequests;
		allowPartial.setToolTip(CreateLang.temporaryText("Allow partial orders")
			.component());
		addRenderableWidget(allowPartial);

		dontAllowPartial = new IconButton(x + 12 + 18, y + bgHeight - 24, AllIcons.I_FULL_REQUESTS);
		dontAllowPartial.withCallback(() -> {
			allowPartial.green = false;
			dontAllowPartial.green = true;
		});
		dontAllowPartial.green = !menu.contentHolder.allowPartialRequests;
		dontAllowPartial.setToolTip(CreateLang.temporaryText("Must send all items")
			.component());
		addRenderableWidget(dontAllowPartial);

		extraAreas = List.of(new Rect2i(x + bgWidth, y + bgHeight - 50, 70, 60));
	}

	@Override
	protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
		int x = getGuiLeft();
		int y = getGuiTop();
		AllGuiTextures.REDSTONE_REQUESTER.render(pGuiGraphics, x, y);
		renderPlayerInventory(pGuiGraphics, x - 3, y + 106);

		Component title = CreateLang.temporaryText("Redstone Requester")
			.component();
		pGuiGraphics.drawString(font, title, x + 100 - font.width(title) / 2, y + 4, 0x3D3C48, false);

		GuiGameElement.of(AllBlocks.REDSTONE_REQUESTER.asStack())
			.scale(3)
			.render(pGuiGraphics, x + 220, y + 60);
	}

	@Override
	protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderForeground(graphics, mouseX, mouseY, partialTicks);
		int x = getGuiLeft();
		int y = getGuiTop();

		for (int i = 0; i < amounts.size(); i++) {
			int inputX = x + 21 + i * 18;
			int inputY = y + 25;
			ItemStack itemStack = menu.ghostInventory.getStackInSlot(i);
			if (itemStack.isEmpty())
				continue;
			PoseStack ms = graphics.pose();
			ms.pushPose();
			ms.translate(0, 0, 100);
			graphics.renderItemDecorations(font, itemStack, inputX, inputY, "" + amounts.get(i));
			ms.popPose();
		}

		if (addressBox.isHovered() && !addressBox.isFocused()) {
			if (addressBox.getValue()
				.isBlank())
				graphics.renderComponentTooltip(font, List.of(CreateLang.temporaryText("Send order to...")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.temporaryText("Enter the address this")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.temporaryText("order should be send to.")
						.style(ChatFormatting.GRAY)
						.component(),
					CreateLang.translate("gui.schedule.lmb_edit")
						.style(ChatFormatting.DARK_GRAY)
						.style(ChatFormatting.ITALIC)
						.component()),
					mouseX, mouseY);
			else
				graphics.renderComponentTooltip(font, List.of(CreateLang.temporaryText("Sending order to")
					.color(ScrollInput.HEADER_RGB)
					.component(),
					CreateLang.temporaryText("'" + addressBox.getValue() + "'")
						.style(ChatFormatting.GRAY)
						.component()),
					mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double pDelta) {
		int x = getGuiLeft();
		int y = getGuiTop();

		for (int i = 0; i < amounts.size(); i++) {
			int inputX = x + 21 + i * 18;
			int inputY = y + 25;
			if (mouseX >= inputX && mouseX < inputX + 16 && mouseY >= inputY && mouseY < inputY + 16) {
				ItemStack itemStack = menu.ghostInventory.getStackInSlot(i);
				if (itemStack.isEmpty())
					return true;
				amounts.set(i,
					Mth.clamp((int) (amounts.get(i) + Math.signum(pDelta) * (hasShiftDown() ? 10 : 1)), 1, 256));
				return true;
			}
		}

		return super.mouseScrolled(mouseX, mouseY, pDelta);
	}

	@Override
	protected List<Component> getTooltipFromContainerItem(ItemStack pStack) {
		List<Component> tooltip = super.getTooltipFromContainerItem(pStack);
		if (!(hoveredSlot instanceof SlotItemHandler))
			return tooltip;

		int slotIndex = this.hoveredSlot.getSlotIndex();
		if (slotIndex >= amounts.size())
			return tooltip;

		return List.of(CreateLang.temporaryText("Send ")
			.add(CreateLang.itemName(pStack)
				.add(CreateLang.text(" x" + amounts.get(slotIndex))))
			.color(ScrollInput.HEADER_RGB)
			.component(),
			CreateLang.temporaryText("Scroll to change amount")
				.style(ChatFormatting.DARK_GRAY)
				.style(ChatFormatting.ITALIC)
				.component(),
			CreateLang.translate("gui.scrollInput.shiftScrollsFaster")
				.style(ChatFormatting.DARK_GRAY)
				.style(ChatFormatting.ITALIC)
				.component());
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

	@Override
	public void removed() {
		AllPackets.getChannel()
			.sendToServer(new RedstoneRequesterConfigurationPacket(menu.contentHolder.getBlockPos(),
				addressBox.getValue(), allowPartial.green, amounts));
		super.removed();
	}

}
