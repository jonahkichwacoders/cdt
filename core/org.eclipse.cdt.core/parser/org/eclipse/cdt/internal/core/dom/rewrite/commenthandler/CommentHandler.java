/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.rewrite.commenthandler;

import java.util.Vector;

import org.eclipse.cdt.core.dom.ast.IASTComment;

/**
 * @author Guido Zgraggen IFS
 * 
 */
public class CommentHandler {

	private final Vector<IASTComment> comments;
	
	public CommentHandler(Vector<IASTComment> comments) {
		super();
		this.comments = comments;
	}

	public void allreadyAdded(IASTComment com) {
		comments.remove(com);
	}

	public boolean hasMore() {
		return comments.size()>0;
	}

	public IASTComment getFirst() {
		return comments.firstElement();
	}
}
