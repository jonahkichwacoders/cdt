/*******************************************************************************
 * Copyright (c) 2020 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.terminal.model;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Colors that can be used in the Terminal are represented by this class. The enum contains
 * the colors with well known names defined by the ANSI Escape Sequences, plus other colors needed
 * to render a display (such as Background color).
 *
 * Rather than name all the colors when using ANSI 8-bit indexed colors, the indexed colors
 * can be accessed via the {@link #getIndexedRGBColor(int)} or {@link #getIndexedRGBColor(int)}
 * (use {@link #isIndexedTerminalColor(int)} to determine which one is appropriate.
 *
 * The {@link TerminalStyle} supports any arbitrary color by using {@link RGB} defined colors.
 * This class provides the connection between the names exposed to the user in preferences
 * and their use in the terminal, along with how colors change when other attributes (such as
 * bright and invertColors) are applied to them.
 *
 * @since 5.0
 */
public enum TerminalColor {
	BLACK("white", new RGB(0, 0, 0), new RGB(0, 0, 0)), //$NON-NLS-1$
	RED(new RGB(205, 0, 0), new RGB(255, 0, 0)), //
	GREEN(new RGB(0, 205, 0), new RGB(0, 255, 0)), //
	YELLOW(new RGB(205, 205, 0), new RGB(255, 255, 0)), //
	BLUE(new RGB(0, 0, 238), new RGB(92, 92, 255)), //
	MAGENTA(new RGB(205, 0, 205), new RGB(255, 0, 255)), //
	CYAN(new RGB(0, 205, 205), new RGB(0, 255, 255)), //
	WHITE("black", new RGB(229, 229, 229), new RGB(255, 255, 255)), //$NON-NLS-1$

	BRIGHT_BLACK("bright_white"), //$NON-NLS-1$
	BRIGHT_RED(), //
	BRIGHT_GREEN(), //
	BRIGHT_YELLOW(), //
	BRIGHT_BLUE(), //
	BRIGHT_MAGENTA(), //
	BRIGHT_CYAN(), //
	BRIGHT_WHITE("bright_black"), //$NON-NLS-1$

	FOREGROUND("background", new RGB(19, 25, 38)), // //$NON-NLS-1$
	BACKGROUND("foreground", new RGB(255, 255, 255)); //$NON-NLS-1$

	/**
	 * This it the prefix that goes in front of {@link TerminalColor}'s enum values (lowercased).
	 */
	private static final String PREF_PREFIX = "terminal.color."; //$NON-NLS-1$
	private static final String BRIGHT_PREFIX = "bright_"; //$NON-NLS-1$

	/**
	 * The first 16-items in the 8-bit lookup table map to the user changeable colors
	 * above, so this array handles that mapping.
	 */
	private final static TerminalColor table8bitIndexedTerminalColors[] = new TerminalColor[16];

	/**
	 * The rest of the colors in the lookup table (240 colors) are pre-defined by
	 * the standard. The colors that fill this table were derived from
	 * https://en.wikipedia.org/wiki/ANSI_escape_code#8-bit which was more
	 * digestible and accessible than the underlying ITU and ISO standards.
	 */
	private final static RGB table8bitIndexedRGB[] = new RGB[256 - 16];

	/**
	 * {@link ColorRegistry} ID in normal mode. Same as {@link #brightId} for already bright colors.
	 */
	private String standardId;

	/**
	 * {@link ColorRegistry} ID in bright mode.
	 */
	private String brightId;

	/**
	 * {@link ColorRegistry} ID in inverted mode. Same as {@link #standardId} for colors that
	 * are not inverted
	 */
	private String invertedId;

	/**
	 * {@link ColorRegistry} ID in inverted bright mode. Same as {@link #invertedId} for
	 * already bright colors.
	 */
	private String invertedBrightId;

	/**
	 * Pre-calculate the lookup tables for 8-bit colors.
	 */
	static {
		TerminalColor[] values = TerminalColor.values();
		int index = 0;
		for (; index < 16; index++) {
			TerminalColor c = values[index];
			table8bitIndexedTerminalColors[index] = c;
		}

		int vals[] = { 0x00, 0x5f, 0x87, 0xaf, 0xd7, 0xff };
		Assert.isTrue(index == 16);
		for (int r = 0; r < 6; r++) {
			for (int g = 0; g < 6; g++) {
				for (int b = 0; b < 6; b++) {
					table8bitIndexedRGB[index++ - 16] = new RGB(vals[r], vals[g], vals[b]);
				}
			}
		}

		int greys[] = { 0x08, 0x12, 0x1c, 0x26, 0x30, 0x3a, 0x44, 0x4e, 0x58, 0x62, 0x6c, 0x76, 0x80, 0x8a, 0x94, 0x9e,
				0xa8, 0xb2, 0xbc, 0xc6, 0xd0, 0xda, 0xe4, 0xee };

		Assert.isTrue(index == 232);
		for (int g : greys) {
			table8bitIndexedRGB[index++ - 16] = new RGB(g, g, g);
		}
		Assert.isTrue(index == 256);
	}

	/**
	 * ANSI colors that are not inverted
	 */
	TerminalColor(RGB defaultStandard, RGB defaultBright) {
		String name = name().toLowerCase();
		this.standardId = this.invertedId = PREF_PREFIX + name;
		this.brightId = this.invertedBrightId = PREF_PREFIX + BRIGHT_PREFIX + name;

		initMissingColor(standardId, defaultStandard);
		initMissingColor(brightId, defaultBright);
	}

	/**
	 * ANSI colors that are inverted
	 */
	TerminalColor(String invertName, RGB defaultStandard, RGB defaultBright) {
		this(defaultStandard, defaultBright);
		this.invertedId = PREF_PREFIX + invertName;
		this.invertedBrightId = PREF_PREFIX + BRIGHT_PREFIX + invertName;
	}

	/**
	 * ANSI bright colors that are not inverted
	 */
	TerminalColor() {
		String name = name().toLowerCase();
		this.standardId = this.invertedId = PREF_PREFIX + name;
		this.brightId = this.invertedBrightId = PREF_PREFIX + name;
	}

	/**
	 * ANSI bright colors that are inverted
	 */
	TerminalColor(String invertName) {
		String name = name().toLowerCase();
		this.standardId = PREF_PREFIX + name;
		this.brightId = PREF_PREFIX + name;
		this.invertedId = PREF_PREFIX + invertName;
		this.invertedBrightId = PREF_PREFIX + invertName;
	}

	/**
	 * Non ANSI defined colors
	 */
	TerminalColor(String invertName, RGB defaultRGB) {
		String name = name().toLowerCase();
		this.standardId = this.brightId = PREF_PREFIX + name;
		this.invertedId = this.invertedBrightId = PREF_PREFIX + invertName;

		initMissingColor(standardId, defaultRGB);
	}

	/**
	 * When run outside of full Eclipse environment, themes and preferences are not
	 * available so we need to provide default colors for the color registry.
	 */
	private void initMissingColor(String id, RGB defaultRGB) {
		if (Display.getCurrent() == null) {
			// Presumably the terminal only exists without a display in the unit tests?
			return;
		}
		ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
		if (colorRegistry.getRGB(id) == null) {
			colorRegistry.put(id, defaultRGB);
		}
	}

	/**
	 * Return a {@link ColorDescriptor} for the given color
	 * with inversions or brightness attributes applied.
	 *
	 * @param invertColors For invertible colors, return the inverse (typically white &lt;-&gt; black)
	 * @param bright returns the brighter version of the color if one is available
	 * @return {@link ColorDescriptor} that a {@link Color} can be made from
	 *     using {@link ColorDescriptor#createColor(org.eclipse.swt.graphics.Device)}
	 * @throws NullPointerException if there is no current {@link Display}
	 */
	public ColorDescriptor getColorDescriptor(boolean invertColors, boolean bright) {
		String colorId;
		if (bright) {
			if (invertColors) {
				colorId = invertedBrightId;
			} else {
				colorId = brightId;
			}
		} else {
			if (invertColors) {
				colorId = invertedId;
			} else {
				colorId = standardId;
			}
		}
		Assert.isNotNull(Display.getCurrent(), "A Display is required to get Color Descriptors"); //$NON-NLS-1$
		return JFaceResources.getColorRegistry().getColorDescriptor(colorId);
	}

	/**
	 * Query for whether the 8-bit color index will return a named color, in which case
	 * {@link #getIndexedTerminalColor(int)} must be called to get the named color. Use
	 * {@link #getColorDescriptor(boolean, boolean)} if this method returns false.
	 *
	 * @param index 8-bit index.
	 * @return true for named colors, false for RGB colors
	 */
	public static boolean isIndexedTerminalColor(int index) {
		Assert.isLegal(index >= 0 && index < 256, "Invalid 8-bit table index out of range 0-255"); //$NON-NLS-1$
		return index < table8bitIndexedTerminalColors.length && index >= 0;
	}

	/**
	 * Return the named color for the given 8-bit index.
	 *
	 * @param index 8-bit index in 0-15 range.
	 * @return named color
	 */
	public static TerminalColor getIndexedTerminalColor(int index) {
		Assert.isLegal(isIndexedTerminalColor(index), "Invalid table index used for ANSI Color"); //$NON-NLS-1$
		return table8bitIndexedTerminalColors[index];
	}

	/**
	 * Return the RGB color for the given 8-bit index.
	 *
	 * @param index 8-bit index in 16-255 range.
	 * @return RGB color
	 */
	public static RGB getIndexedRGBColor(int index) {
		Assert.isLegal(index >= 16 && index < 256, "Invalid table index used for RGB Color"); //$NON-NLS-1$
		return table8bitIndexedRGB[index - 16];
	}

}
