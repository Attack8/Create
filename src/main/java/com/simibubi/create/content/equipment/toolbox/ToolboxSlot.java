package com.simibubi.create.content.equipment.toolbox;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ToolboxSlot extends SlotItemHandler {

	private ToolboxMenu toolboxMenu;

	public ToolboxSlot(ToolboxMenu menu, IItemHandler itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
		this.toolboxMenu = menu;
	}

	@Override
	public boolean isActive() {
		return !toolboxMenu.renderPass && super.isActive();
	}

}
