/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.qt.core;

import java.nio.file.Path;

/**
 * Represents an installation of the Qt SDK. Qt installs are defined by the path
 * to the qmake executable.
 * 
 * @noimplement
 */
public interface IQtInstall {

	String getName();

	Path getQmakePath();

	String getSpec();

	boolean supports(String os, String arch);

	Path getLibPath();

}
