/**********************************************************************
 * Copyright (c) 2003,2004 IBM Rational Software and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Configuration extends BuildObject implements IConfiguration {

	private ITarget target;
	private IConfiguration parent;
	private List toolReferences;
	private boolean resolved = true;

	/**
	 * Build a configuration from the project manifest file.
	 * 
	 * @param target The <code>Target</code> the configuration belongs to. 
	 * @param element The element from the manifest that contains the overridden configuration information.
	 */
	public Configuration(Target target, Element element) {
		this.target = target;
		
		// id
		setId(element.getAttribute(IConfiguration.ID));
		
		// hook me up
		target.addConfiguration(this);
		
		// name
		if (element.hasAttribute(IConfiguration.NAME))
			setName(element.getAttribute(IConfiguration.NAME));
		
		if (element.hasAttribute(IConfiguration.PARENT)) {
			// See if the target has a parent
			ITarget targetParent = target.getParent();
			// If so, then get my parent from it
			if (targetParent != null) {
				parent = targetParent.getConfiguration(element.getAttribute(IConfiguration.PARENT));
			}
			else {
				parent = null;
			}
		}
		
		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals(IConfiguration.TOOLREF_ELEMENT_NAME)) {
				new ToolReference(this, (Element)configElement);
			}
		}
	
	}

	/**
	 * Create a new configuration based on one already defined.
	 * 
	 * @param target The <code>Target</code> the receiver will be added to.
	 * @param parent The <code>IConfiguration</code> to copy the settings from.
	 * @param id A unique ID for the configuration.
	 */
	public Configuration(Target target, IConfiguration parent, String id) {
		this.id = id;
		this.name = parent.getName();
		this.target = target;
		this.parent = parent;
		
		// Check that the tool and the project match
		IProject project = (IProject) target.getOwner();
		
		// Get the tool references from the parent
		List parentToolRefs = ((Configuration)parent).getLocalToolReferences();
		Iterator iter = parentToolRefs.listIterator();
		while (iter.hasNext()) {
			ToolReference toolRef = (ToolReference)iter.next();

			// Make a new ToolReference based on the tool in the ref
			ToolReference newRef = new ToolReference(this, toolRef.getTool());
			List optRefs = toolRef.getOptionReferenceList();
			Iterator optIter = optRefs.listIterator();
			while (optIter.hasNext()) {
				OptionReference optRef = (OptionReference)optIter.next();
				IOption opt = optRef.getOption();
				try {
					switch (opt.getValueType()) {
						case IOption.BOOLEAN:
							new OptionReference(newRef, opt).setValue(optRef.getBooleanValue());
							break;
						case IOption.STRING:
							new OptionReference(newRef, opt).setValue(optRef.getStringValue());
							break;
						case IOption.ENUMERATED:
							new OptionReference(newRef, opt).setValue(optRef.getSelectedEnum());
							break;
						case IOption.STRING_LIST :
							new OptionReference(newRef, opt).setValue(optRef.getStringListValue());
							break;
						case IOption.INCLUDE_PATH :
							new OptionReference(newRef, opt).setValue(optRef.getIncludePaths());
							break;
						case IOption.PREPROCESSOR_SYMBOLS :
							new OptionReference(newRef, opt).setValue(optRef.getDefinedSymbols());
							break;
						case IOption.LIBRARIES :
						new OptionReference(newRef, opt).setValue(optRef.getLibraries());
							break;
						case IOption.OBJECTS :
						new OptionReference(newRef, opt).setValue(optRef.getUserObjects());
							break;
					}
				} catch (BuildException e) {
					continue;
				}
			}
		}
		
		target.addConfiguration(this);
	}

	/**
	 * Create a new <code>Configuration</code> based on the specification in the plugin manifest.
	 * 
	 * @param target The <code>Target</code> the receiver will be added to.
	 * @param element The element from the manifest that contains the default configuration settings.
	 */
	public Configuration(Target target, IConfigurationElement element) {
		this.target = target;
		
		// setup for resolving
		ManagedBuildManager.putConfigElement(this, element);
		resolved = false;
		
		// id
		setId(element.getAttribute(IConfiguration.ID));
		
		// hook me up
		target.addConfiguration(this);
		
		// name
		setName(element.getAttribute(IConfiguration.NAME));

		IConfigurationElement[] configElements = element.getChildren();
		for (int l = 0; l < configElements.length; ++l) {
			IConfigurationElement configElement = configElements[l];
			if (configElement.getName().equals(IConfiguration.TOOLREF_ELEMENT_NAME)) {
				new ToolReference(this, configElement);
			}
		}
	}
	
	/**
	 * A fresh new configuration for a target.
	 * 
	 * @param target
	 * @param id
	 */	
	public Configuration(Target target, String id) {
		this.id = id;
		this.target = target;
		
		target.addConfiguration(this);
	}

	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
//			IConfigurationElement element = ManagedBuildManager.getConfigElement(this);
			Iterator refIter = getLocalToolReferences().iterator();
			while (refIter.hasNext()) {
				ToolReference ref = (ToolReference)refIter.next();
				ref.resolveReferences();
			}
		}
	}
	
	/**
	 * Adds a tool reference to the receiver.
	 * 
	 * @param toolRef
	 */
	public void addToolReference(ToolReference toolRef) {
		getLocalToolReferences().add(toolRef);
	}
	
	/* (non-Javadoc)
	 * @param option
	 * @return
	 */
	private OptionReference createOptionReference(IOption option) {
		if (option instanceof OptionReference) {
			OptionReference optionRef = (OptionReference)option;
			ToolReference toolRef = optionRef.getToolReference();
			if (toolRef.ownedByConfiguration(this))
				return optionRef;
			else {
				toolRef = new ToolReference(this, toolRef);
				return toolRef.createOptionReference(option);
			}
		} else {
			ToolReference toolRef = getToolReference(option.getTool());
			if (toolRef == null)
				toolRef = new ToolReference(this, option.getTool());
			return toolRef.createOptionReference(option);
		}
	}

	/* (non-javadoc)
	 * A safety method to avoid NPEs. It answers the tool reference list in the 
	 * receiver. It does not look at the tool references defined in the parent.
	 * 
	 * @return List
	 */
	protected List getLocalToolReferences() {
		if (toolReferences == null) {
			toolReferences = new ArrayList();
		}
		return toolReferences;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getName()
	 */
	public String getName() {
		return (name == null && parent != null) ? parent.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getTools()
	 */
	public ITool[] getTools() {
		ITool[] tools = parent != null
			? parent.getTools()
			: target.getTools();
		
		// Validate that the tools correspond to the nature
		IProject project = (IProject)target.getOwner();
		if (project != null) {
			List validTools = new ArrayList();
			
			// The target is associated with a real project
			for (int i = 0; i < tools.length; ++i) {
				ITool tool = tools[i];
				// Make sure the tool filter and project nature agree
				switch (tool.getNatureFilter()) {
					case ITool.FILTER_C:
						try {
							if (project.hasNature(CProjectNature.C_NATURE_ID) && !project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								validTools.add(tool);
							}
						} catch (CoreException e) {
							continue;
						}
						break;
					case ITool.FILTER_CC:
						try {
							if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
								validTools.add(tool);
							}
						} catch (CoreException e) {
							continue;
						}
						break;
					case ITool.FILTER_BOTH:
						validTools.add(tool);
						break;
				} 
			}
			// Now put the valid tools back into the array
			tools = (ITool[]) validTools.toArray(new ITool[validTools.size()]);			
		}
		
		// Replace tools with local overrides
		for (int i = 0; i < tools.length; ++i) {
			ToolReference ref = getToolReference(tools[i]);
			if (ref != null)
				tools[i] = ref;
		}
		
		return tools;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getParent()
	 */
	public IConfiguration getParent() {
		return parent;
	}
	
	/* (non-javadoc)
	 * 
	 * @param tool
	 * @return List
	 */
	protected List getOptionReferences(ITool tool) {
		List references = new ArrayList();
		
		// Get all the option references I add for this tool
		ToolReference toolRef = getToolReference(tool);
		if (toolRef != null) {
			references.addAll(toolRef.getOptionReferenceList());
		}
		
		// See if there is anything that my parents add that I don't
		if (parent != null) {
			List temp = ((Configuration)parent).getOptionReferences(tool);
			Iterator iter = temp.listIterator();
			while (iter.hasNext()) {
				OptionReference ref = (OptionReference) iter.next();
				if (!references.contains(ref)) {
					references.add(ref);
				}
			}
		}
		
		return references;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getTarget()
	 */
	public ITarget getTarget() {
		return (target == null && parent != null) ? parent.getTarget() : target;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#getOwner()
	 */
	public IResource getOwner() {
		return getTarget().getOwner();
	}

	/* (non-Javadoc)
	 * Returns the reference for a given tool or <code>null</code> if one is not
	 * found.
	 * 
	 * @param tool
	 * @return ToolReference
	 */
	private ToolReference getToolReference(ITool tool) {
		// See if the receiver has a reference to the tool
		ToolReference ref = null;
		if (tool == null) return ref;
		Iterator iter = getLocalToolReferences().listIterator();
		while (iter.hasNext()) {
			ToolReference temp = (ToolReference)iter.next(); 
			if (temp.references(tool)) {
				ref = temp;
				break;
			}
		}
		return ref;
	}
	
	/**
	 * @param targetElement
	 */
	public void reset(IConfigurationElement element) {
		// I just need to reset the tool references
		getLocalToolReferences().clear();
		IConfigurationElement[] configElements = element.getChildren();
		for (int l = 0; l < configElements.length; ++l) {
			IConfigurationElement configElement = configElements[l];
			if (configElement.getName().equals(IConfiguration.TOOLREF_ELEMENT_NAME)) {
				ToolReference ref = new ToolReference(this, configElement);
				ref.resolveReferences();
			}
		}
	}

	/**
	 * Persist receiver to project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		element.setAttribute(IConfiguration.ID, id);
		
		if (name != null)
			element.setAttribute(IConfiguration.NAME, name);
			
		if (parent != null)
			element.setAttribute(IConfiguration.PARENT, parent.getId());
		
		// Serialize only the tool references defined in the configuration
		Iterator iter = getLocalToolReferences().listIterator();
		while (iter.hasNext()) {
			ToolReference toolRef = (ToolReference) iter.next();
			Element toolRefElement = doc.createElement(IConfiguration.TOOLREF_ELEMENT_NAME);
			element.appendChild(toolRefElement);
			toolRef.serialize(doc, toolRefElement);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setOption(org.eclipse.cdt.core.build.managed.IOption, boolean)
	 */
	public void setOption(IOption option, boolean value) throws BuildException {
		// Is there a delta
		if (option.getBooleanValue() != value)
			createOptionReference(option).setValue(value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setOption(org.eclipse.cdt.core.build.managed.IOption, java.lang.String)
	 */
	public void setOption(IOption option, String value) throws BuildException {
		String oldValue;
		// Check whether this is an enumerated option
		if (option.getValueType() == IOption.ENUMERATED) {
			oldValue = option.getSelectedEnum();
		}
		else {
			oldValue = option.getStringValue(); 
		}
		if (oldValue != null && !oldValue.equals(value))
			createOptionReference(option).setValue(value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IConfiguration#setOption(org.eclipse.cdt.core.build.managed.IOption, java.lang.String[])
	 */
	public void setOption(IOption option, String[] value) throws BuildException {
		// Is there a delta
		String[] oldValue;
		switch (option.getValueType()) {
			case IOption.STRING_LIST :
				oldValue = option.getStringListValue();
				break;
			case IOption.INCLUDE_PATH :
				oldValue = option.getIncludePaths();
				break;
			case IOption.PREPROCESSOR_SYMBOLS :
				oldValue = option.getDefinedSymbols();
				break;
			case IOption.LIBRARIES :
				oldValue = option.getLibraries();
				break;
			case IOption.OBJECTS :
				oldValue = option.getUserObjects();
				break;
			default :
				oldValue = new String[0];
				break;
		}
		if(!Arrays.equals(value, oldValue))
			createOptionReference(option).setValue(value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IConfiguration#setToolCommand(org.eclipse.cdt.managedbuilder.core.ITool, java.lang.String)
	 */
	public void setToolCommand(ITool tool, String command) {
		// Make sure the command is different
		if (command != null) {
			// Does this config have a ref to the tool
			ToolReference ref = getToolReference(tool);
			if (ref == null) {
				// Then make one
				ref = new ToolReference(this, tool);
			}
			// Set the ref's command
			if (ref != null) {
				ref.setToolCommand(command);
			}
		}
	}
}
