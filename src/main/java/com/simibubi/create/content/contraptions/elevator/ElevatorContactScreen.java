package com.simibubi.create.content.contraptions.elevator;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.decoration.slidingDoor.DoorControl;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Label;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.simibubi.create.foundation.gui.widget.TooltipArea;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.data.Pair;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

public class ElevatorContactScreen extends AbstractSimiScreen {

	private AllGuiTextures background;

	private EditBox shortNameInput;
	private EditBox longNameInput;
	private IconButton confirm;

	private String shortName;
	private String longName;
	private DoorControl doorControl;

	private BlockPos pos;

	public ElevatorContactScreen(BlockPos pos, String prevShortName, String prevLongName, DoorControl prevDoorControl) {
		super(CreateLang.translateDirect("elevator_contact.title"));
		this.pos = pos;
		this.doorControl = prevDoorControl;
		background = AllGuiTextures.ELEVATOR_CONTACT;
		this.shortName = prevShortName;
		this.longName = prevLongName;
	}

	@Override
	public void init() {
		setWindowSize(background.getWidth() + 30, background.getHeight());
		super.init();

		int x = guiLeft;
		int y = guiTop;

		confirm = new IconButton(x + 200, y + 58, AllIcons.I_CONFIRM);
		confirm.withCallback(this::confirm);
		addRenderableWidget(confirm);

		shortNameInput = editBox(33, 30, 4);
		shortNameInput.setValue(shortName);
		centerInput(x);
		shortNameInput.setResponder(s -> {
			shortName = s;
			centerInput(x);
		});
		shortNameInput.setFocused(true);
		setFocused(shortNameInput);
		shortNameInput.setHighlightPos(0);

		longNameInput = editBox(63, 140, 30);
		longNameInput.setValue(longName);
		longNameInput.setResponder(s -> longName = s);

		MutableComponent rmbToEdit = CreateLang.translate("gui.schedule.lmb_edit")
			.style(ChatFormatting.DARK_GRAY)
			.style(ChatFormatting.ITALIC)
			.component();

		addRenderableOnly(new TooltipArea(x + 21, y + 23, 30, 18)
			.withTooltip(ImmutableList.of(CreateLang.translate("elevator_contact.floor_identifier")
				.color(0x5391E1)
				.component(), rmbToEdit)));

		addRenderableOnly(new TooltipArea(x + 57, y + 23, 147, 18).withTooltip(ImmutableList.of(
			CreateLang.translate("elevator_contact.floor_description")
				.color(0x5391E1)
				.component(),
			CreateLang.translate("crafting_blueprint.optional")
				.style(ChatFormatting.GRAY)
				.component(),
			rmbToEdit)));

		Pair<ScrollInput, Label> doorControlWidgets =
			DoorControl.createWidget(x + 58, y + 57, mode -> doorControl = mode, doorControl);
		addRenderableWidget(doorControlWidgets.getFirst());
		addRenderableWidget(doorControlWidgets.getSecond());
	}

	private int centerInput(int x) {
		int centeredX = x + (shortName.isEmpty() ? 34 : 36 - font.width(shortName) / 2);
		shortNameInput.setX(centeredX);
		return centeredX;
	}

	private EditBox editBox(int x, int width, int chars) {
		EditBox editBox = new EditBox(font, guiLeft + x, guiTop + 30, width, 10, CommonComponents.EMPTY);
		editBox.setTextColor(-1);
		editBox.setTextColorUneditable(-1);
		editBox.setBordered(false);
		editBox.setMaxLength(chars);
		editBox.setFocused(false);
		editBox.mouseClicked(0, 0, 0);
		addRenderableWidget(editBox);
		return editBox;
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(graphics, x, y);

		FormattedCharSequence formattedcharsequence = title.getVisualOrderText();
		graphics.drawString(font, formattedcharsequence,
			(float) (x + (background.getWidth() - 8) / 2 - font.width(formattedcharsequence) / 2), (float) y + 6, 0x2F3738, false);

		GuiGameElement.of(AllBlocks.ELEVATOR_CONTACT.asStack()).<GuiGameElement
				.GuiRenderBuilder>at(x + background.getWidth() + 6, y + background.getHeight() - 56, -200)
			.scale(5)
			.render(graphics);

		graphics.renderItem(AllBlocks.TRAIN_DOOR.asStack(), x + 37, y + 58);
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		boolean consumed = super.mouseClicked(pMouseX, pMouseY, pButton);

		if (!shortNameInput.isFocused()) {
			int length = shortNameInput.getValue()
				.length();
			shortNameInput.setHighlightPos(length);
			shortNameInput.setCursorPosition(length);
		}

		if (shortNameInput.isHoveredOrFocused())
			longNameInput.mouseClicked(0, 0, 0);

		if (!consumed && pMouseX > guiLeft + 22 && pMouseY > guiTop + 24 && pMouseX < guiLeft + 50
			&& pMouseY < guiTop + 40) {
			setFocused(shortNameInput);
			shortNameInput.setFocused(true);
			return true;
		}

		return consumed;
	}

	@Override
	public boolean keyPressed(int keyCode, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (super.keyPressed(keyCode, p_keyPressed_2_, p_keyPressed_3_))
			return true;
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			confirm();
			return true;
		}
		if (keyCode == 256 && this.shouldCloseOnEsc()) {
			this.onClose();
			return true;
		}
		return false;
	}

	private void confirm() {
		AllPackets.getChannel()
			.sendToServer(new ElevatorContactEditPacket(pos, shortName, longName, doorControl));
		onClose();
	}

}
