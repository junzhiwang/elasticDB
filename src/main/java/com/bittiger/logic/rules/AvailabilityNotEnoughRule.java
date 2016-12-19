package com.bittiger.logic.rules;

import org.easyrules.annotation.Action;
import org.easyrules.annotation.Condition;
import org.easyrules.annotation.Rule;

import com.bittiger.client.Utilities;
import com.bittiger.logic.ActionType;
import com.bittiger.logic.Controller;

@Rule(name = "AvailabilityRule", description = "Guarrantee the minimum number of slaves")
public class AvailabilityNotEnoughRule {

	private Controller controller;

	@Condition
	public boolean checkNumSlave() {
		// The rule should be applied only if
		// the number of slaves is less than minimum.
		return controller.getReadQueue().size() < Utilities.minimumSlave;
	}

	@Action
	public void increase() throws Exception {
		controller.getEventQueue().put(ActionType.AvailNotEnoughAddServer);
	}

	public void setInput(Controller controller) {
		this.controller = controller;
	}

}
