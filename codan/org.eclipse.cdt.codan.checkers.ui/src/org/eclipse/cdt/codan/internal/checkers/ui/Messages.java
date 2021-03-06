/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui;

import org.eclipse.osgi.util.NLS;

/**
 * Localizable messages.
 */
public class Messages extends NLS {
	public static String CatchByReferenceQuickFix_Message;
	public static String CatchByConstReferenceQuickFix_Message;
	public static String QuickFixAssignmentInCondition_Message;
	public static String SuggestedParenthesisQuickFix_Message;

	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	// Do not instantiate
	private Messages() {
	}
}
