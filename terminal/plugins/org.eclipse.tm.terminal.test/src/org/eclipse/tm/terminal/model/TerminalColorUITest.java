/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *******************************************************************************/
package org.eclipse.tm.terminal.model;

import static org.eclipse.tm.terminal.model.TerminalColor.BLACK;
import static org.eclipse.tm.terminal.model.TerminalColor.BLUE;
import static org.eclipse.tm.terminal.model.TerminalColor.CYAN;
import static org.eclipse.tm.terminal.model.TerminalColor.GREEN;
import static org.eclipse.tm.terminal.model.TerminalColor.MAGENTA;
import static org.eclipse.tm.terminal.model.TerminalColor.RED;
import static org.eclipse.tm.terminal.model.TerminalColor.WHITE;
import static org.eclipse.tm.terminal.model.TerminalColor.YELLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.swt.widgets.Display;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is a UI test because {@link TerminalColor#getColorDescriptor(boolean, boolean)
 * requires a Display to operate the ColorRegistry.
 */
public class TerminalColorUITest {

	private static Display display = null;

	@BeforeClass
	public static void createDisplay() {
		Display current = Display.getCurrent();
		if (current == null) {
			display = new Display();
		}
	}

	@AfterClass
	public static void disposeDisplay() {
		if (display != null) {
			display.dispose();
		}
	}

	@Test
	public void testInversionsStandard() {

		assertEquals(BLACK.getColorDescriptor(false, false), WHITE.getColorDescriptor(true, false));
		assertNotEquals(BLACK.getColorDescriptor(false, false), WHITE.getColorDescriptor(false, false));

		assertEquals(RED.getColorDescriptor(false, false), RED.getColorDescriptor(true, false));
		assertEquals(GREEN.getColorDescriptor(false, false), GREEN.getColorDescriptor(true, false));
		assertEquals(YELLOW.getColorDescriptor(false, false), YELLOW.getColorDescriptor(true, false));
		assertEquals(BLUE.getColorDescriptor(false, false), BLUE.getColorDescriptor(true, false));
		assertEquals(MAGENTA.getColorDescriptor(false, false), MAGENTA.getColorDescriptor(true, false));
		assertEquals(CYAN.getColorDescriptor(false, false), CYAN.getColorDescriptor(true, false));

		assertEquals(WHITE.getColorDescriptor(false, false), BLACK.getColorDescriptor(true, false));
		assertNotEquals(WHITE.getColorDescriptor(false, false), BLACK.getColorDescriptor(false, false));

	}

	@Test
	public void testInversionsBright() {
		assertEquals(BLACK.getColorDescriptor(false, true), WHITE.getColorDescriptor(true, true));
		assertNotEquals(BLACK.getColorDescriptor(false, true), WHITE.getColorDescriptor(false, true));

		assertEquals(RED.getColorDescriptor(false, true), RED.getColorDescriptor(true, true));
		assertEquals(GREEN.getColorDescriptor(false, true), GREEN.getColorDescriptor(true, true));
		assertEquals(YELLOW.getColorDescriptor(false, true), YELLOW.getColorDescriptor(true, true));
		assertEquals(BLUE.getColorDescriptor(false, true), BLUE.getColorDescriptor(true, true));
		assertEquals(MAGENTA.getColorDescriptor(false, true), MAGENTA.getColorDescriptor(true, true));
		assertEquals(CYAN.getColorDescriptor(false, true), CYAN.getColorDescriptor(true, true));

		assertEquals(WHITE.getColorDescriptor(false, true), BLACK.getColorDescriptor(true, true));
		assertNotEquals(WHITE.getColorDescriptor(false, true), BLACK.getColorDescriptor(false, true));
	}

	@Test
	public void testIndexesResolveToStandardColors() {
		// check explicit colors
		assertEquals(TerminalColor.BLACK.getColorDescriptor(false, false),
				TerminalColor.getIndexedTerminalColor(0).getColorDescriptor(false, false));
		assertEquals(TerminalColor.RED.getColorDescriptor(false, false),
				TerminalColor.getIndexedTerminalColor(1).getColorDescriptor(false, false));

		// Now check all colors
		for (int i = 0; i < 8; i++) {
			assertEquals(TerminalColor.values()[i].getColorDescriptor(false, false),
					TerminalColor.getIndexedTerminalColor(i).getColorDescriptor(false, false));
		}
	}

	@Test
	public void testIndexesResolveToBrightColors() {
		// check explicit colors
		assertEquals(TerminalColor.BLACK.getColorDescriptor(false, true),
				TerminalColor.getIndexedTerminalColor(8).getColorDescriptor(false, false));
		assertEquals(TerminalColor.RED.getColorDescriptor(false, true),
				TerminalColor.getIndexedTerminalColor(9).getColorDescriptor(false, false));

		// Now check all colors
		for (int i = 0; i < 8; i++) {
			assertEquals(TerminalColor.values()[i].getColorDescriptor(false, true),
					TerminalColor.getIndexedTerminalColor(i + 8).getColorDescriptor(false, false));
		}
	}

	@Test
	public void testIndexesInRange() {
		for (int i = 0; i < 16; i++) {
			assertNotNull(TerminalColor.getIndexedTerminalColor(i));
		}
		for (int i = 16; i < 256; i++) {
			assertNotNull(TerminalColor.getIndexedRGBColor(i));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIndexesOutOfRange_m1TerminalColor() {
		assertNotNull(TerminalColor.getIndexedTerminalColor(-1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIndexesOutOfRange_m1RGBColor() {
		assertNotNull(TerminalColor.getIndexedRGBColor(-1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIndexesOutOfRange_16() {
		assertNotNull(TerminalColor.getIndexedTerminalColor(16));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIndexesOutOfRange_15() {
		assertNotNull(TerminalColor.getIndexedRGBColor(15));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIndexesOutOfRange_256TerminalColor() {
		assertNotNull(TerminalColor.getIndexedTerminalColor(256));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIndexesOutOfRange_256RGBColor() {
		assertNotNull(TerminalColor.getIndexedRGBColor(256));
	}

}
