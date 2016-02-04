/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 * @contributor Victor Perez , e-Evolution.SC FR [ 1757088 ]                  *
 *****************************************************************************/
package org.compiere.apps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.adempiere.ad.persistence.TableModelLoader;
import org.adempiere.ad.security.IUserRolePermissions;
import org.adempiere.ad.trx.api.ITrx;
import org.adempiere.exceptions.AdempiereException;
import org.adempiere.model.CopyRecordFactory;
import org.adempiere.model.CopyRecordSupport;
import org.adempiere.model.InterfaceWrapperHelper;
import org.adempiere.model.TableInfoVO;
import org.adempiere.plaf.AdempierePLAF;
import org.adempiere.plaf.VPanelUI;
import org.adempiere.process.event.IProcessEventListener;
import org.adempiere.process.event.IProcessEventSupport;
import org.adempiere.process.event.ProcessEvent;
import org.adempiere.util.Check;
import org.adempiere.util.Services;
import org.adempiere.util.api.IMsgBL;
import org.compiere.apps.form.FormFrame;
import org.compiere.apps.search.Find;
import org.compiere.apps.search.FindPanelContainer;
import org.compiere.apps.search.InfoWindowMenuBuilder;
import org.compiere.grid.APanelTab;
import org.compiere.grid.GridController;
import org.compiere.grid.GridSynchronizer;
import org.compiere.grid.ICreateFrom;
import org.compiere.grid.RecordAccessDialog;
import org.compiere.grid.VCreateFromFactory;
import org.compiere.grid.VOnlyCurrentDays;
import org.compiere.grid.VPayment;
import org.compiere.grid.VSortTab;
import org.compiere.grid.VTabbedPane;
import org.compiere.grid.ed.VButton;
import org.compiere.grid.ed.VDocAction;
import org.compiere.model.DataStatusEvent;
import org.compiere.model.DataStatusListener;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.GridTab.DataNewCopyMode;
import org.compiere.model.GridTabMaxRows;
import org.compiere.model.GridTabMaxRowsRestrictionChecker;
import org.compiere.model.GridTable;
import org.compiere.model.GridWindow;
import org.compiere.model.GridWindowVO;
import org.compiere.model.GridWorkbench;
import org.compiere.model.I_AD_Form;
import org.compiere.model.I_AD_Window;
import org.compiere.model.Lookup;
import org.compiere.model.MLookupFactory;
import org.compiere.model.MQuery;
import org.compiere.model.MWindow;
import org.compiere.model.PO;
import org.compiere.print.AReport;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessClassInfo;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoUtil;
import org.compiere.swing.CPanel;
import org.compiere.util.ASyncProcess;
import org.compiere.util.CLogMgt;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Util;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import de.metas.adempiere.form.IClientUI;
import de.metas.adempiere.model.I_AD_Process;
/**
 *	Main Panel of application window.
 *  <pre>
 *  Structure:
 *      (MenuBar) -> to be added to owning window
 *		northPanel  (ToolBar)
 *		tabPanel
 *		southPanel  (StatusBar)
 *  </pre>
 *
 * 	@author 	Jorg Janke
 * 	@version 	$Id: APanel.java,v 1.4 2006/07/30 00:51:27 jjanke Exp $
 * 
 *  Colin Rooney 2007/03/20 RFE#1670185 & related BUG#1684142 - Extend Sec to Info Queries
 *  @contributor Victor Perez , e-Evolution.SC FR [ 1757088 ]
 *  @contributor fer_luck@centuryon.com , FR [ 1757088 ]
 *  @author Teo Sarca, SC ARHIPAC SERVICE SRL
 *				<li>BF [ 1824621 ] History button can't be canceled
 *				<li>BF [ 1941271 ] VTreePanel is modifying even if is save wasn't successfull
 *				<li>FR [ 1943731 ] Window data export functionality
 *				<li>FR [ 1974354 ] VCreateFrom.create should be more flexible
 * 				<li>BF [ 1996056 ] Report error message is not displayed
 * 				<li>BF [ 1998575 ] Document Print is discarding any error
 *  @author Teo Sarca, teo.sarca@gmail.com
 *  			<li>BF [ 2876892 ] Save included tab before calling button action
 *  				https://sourceforge.net/tracker/?func=detail&aid=2876892&group_id=176962&atid=879332
 *  @author victor.perez@e-evolution.com 
 *  @see FR [ 1966328 ] New Window Info to MRP and CRP into View http://sourceforge.net/tracker/index.php?func=detail&aid=1966328&group_id=176962&atid=879335
 *  @autor tobi42, metas GmBH
 *  			<li>BF [ 2799362 ] You can press New button a lot of times
 *  @author Cristina Ghita, www.arhipac.ro
 *  @see FR [ 2877111 ] See identifiers columns when delete records https://sourceforge.net/tracker/?func=detail&atid=879335&aid=2877111&group_id=176962
 * 
 * 	@author hengsin, hengsin.low@idalica.com
 *  @see FR [2887701] https://sourceforge.net/tracker/?func=detail&atid=879335&aid=2887701&group_id=176962
 *  @sponsor www.metas.de
 */
// metas: removed final modifier
public class APanel extends CPanel
	implements DataStatusListener
	, ChangeListener
	, ActionListener
	, ASyncProcess
	, IProcessEventListener // metas
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6066778919781303581L;
	
	private static final String UIKEY_AlignVerticalTabsWithHorizontalTabs = "APanel.AlignVerticalTabsWithHorizontalTabs";
	private boolean alignVerticalTabsWithHorizontalTabs = false;

	/**
	 * Included tab constructor.
	 * 
	 * Need to call initPanel for dynamic initialization
	 */
	//FR [ 1757088 ]
	public APanel(final GridController gc, final int windowNo)
	{
		super();
		isNested = true;
		m_ctx = Env.getCtx();
		m_window = null; // no window
		
		try
		{
			m_curGC = gc;
			gc.addDataStatusListener(this);
			m_curTab = gc.getMTab();
			
			final Component tabElement = gc;
			final VTabbedPane tabPane = new VTabbedPane(false);
			tabPane.addTab(m_curTab.getName(), m_curTab, tabElement);
			m_curWinTab = tabPane;
			m_curWindowNo = windowNo;
			isTabIncluded = true; // metas-2009_0021_AP1_CR056
			
			jbInit();
			initSwitchLineAction();
		}
		catch(Exception e)
		{
			log.log(Level.SEVERE, "", e);
		}
		
		createMenuAndToolbar();
		
		m_curGC.query (m_onlyCurrentRows, m_onlyCurrentDays, GridTabMaxRows.DEFAULT);
		m_curTab.navigateCurrent();     //  updates counter
		m_curGC.dynamicDisplay(0);
		Services.get(IProcessEventSupport.class).addListener(this); // metas: add this instance as a listener
	}
	
	public APanel(final AWindow window)
	{
		super();
		m_ctx = Env.getCtx();
		isNested = false;
		m_window = window;
		alignVerticalTabsWithHorizontalTabs = AdempierePLAF.getBoolean(UIKEY_AlignVerticalTabsWithHorizontalTabs, true);
		tabPanel.setAlignVerticalTabsWithHorizontalTabs(alignVerticalTabsWithHorizontalTabs);
				
		//
		try
		{
			jbInit();
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "", e);
		}
		createMenuAndToolbar();
		Services.get(IProcessEventSupport.class).addListener(this); // metas: add this instance as a listener
	}	//	APanel

	/**	Logger			*/
	private static final CLogger log = CLogger.getCLogger(APanel.class);
	
	/** true if this component will be embedded in some tabs (i.e. included tabs) */
	private final boolean isNested;
	private final AWindow m_window;
	private boolean isCancel = false; //Goodwill

	/**
	 *	Dispose
	 */
	public void dispose()
	{
	//	log.config("");
		//  ignore changes
		m_disposing = true;
		//
		if (m_curAPanelTab != null)
		{
			m_curAPanelTab.unregisterPanel();
			m_curAPanelTab = null;
		}
		//  close panels
		if (tabPanel != null)
		{
			tabPanel.dispose(this);
			tabPanel = null;
		}
		
		//  All Workbenches
		if (m_mWorkbench != null)
		{
			for (int i = 0; i < m_mWorkbench.getWindowCount(); i++)
			{
				m_curWindowNo = m_mWorkbench.getWindowNo(i);
				log.info("#" + m_curWindowNo);
				Env.setAutoCommit(m_ctx, m_curWindowNo, false);
				m_mWorkbench.dispose(i);
				Env.clearWinContext(m_ctx, m_curWindowNo);
			}
			m_mWorkbench.dispose();
			m_mWorkbench = null;
		}
		//  MenuBar
		if (menuBar != null)
		{
			menuBar.removeAll();
			menuBar = null;
		}
		//  ToolBar
		if (toolBar != null)
		{
			toolBar.removeAll();
			toolBar = null;
			getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
		}

		// Current grid controller
		// mainly, it shall be already disposed, but we do this for included tab case where tabPanels is null.
		if (m_curGC != null)
		{
			m_curGC.removeDataStatusListener(this);
			m_curGC.dispose();
			m_curGC = null;
		}
		
		//  Prepare GC
		this.removeAll();
		
		Services.get(IProcessEventSupport.class).removeListener(this); // metas: remove this instance a a listener
	}	//	dispose

	private VTabbedPane tabPanel = new VTabbedPane(true);
	private StatusBar statusBar = new StatusBar();
	private JToolBar toolBar = new JToolBar();
	private JMenuBar menuBar = new JMenuBar();

	/**
	 * Initializes the state of this instance.
	 * @throws Exception
	 */
	private final void jbInit() throws Exception
	{
		//
		// Components
		{
			this.setLocale(Env.getLanguage(m_ctx).getLocale());
		}

		//
		// Layout
		{
			final BorderLayout mainLayout = new BorderLayout();
			mainLayout.setHgap(2);
			mainLayout.setVgap(2);
			this.setLayout(mainLayout);

			//
			// North: toolbar
			{
				// NOTE: we are wrapping it in a container panel, to make sure all the buttons are aligned on left and they don't grow.
				final CPanel toolbarContainer = new CPanel();
				toolbarContainer.setLayout(new FlowLayout(FlowLayout.LEFT));
				toolbarContainer.add(toolBar);

				this.add(toolbarContainer, BorderLayout.NORTH);
			}

			//
			// Center: tabs panel
			if (isNested)
			{
				this.add(m_curGC, BorderLayout.CENTER);
				tabPanel = null; // make sure nobody is using this tabPanel because we are not adding it to our UI... so it would misleading to use it.
			}
			else
			{
				this.add(tabPanel, BorderLayout.CENTER);
			}

			//
			// South: status bar
			if (!isTabIncluded) // metas: only add the status bar, if this is not an included tab's panel
			{
				this.add(statusBar, BorderLayout.SOUTH);
			}
		}
	}	// jbInit

	private AppsAction 		aPrevious, aNext, aParent, aDetail, aFirst, aLast,
							aNew, aCopy, aDelete, aPrint, aPrintPreview,
							aExport = null,
							aRefresh, aHistory, aAttachment, aChat, aFind,
							aWorkflow, aZoomAcross, aRequest, aWinSize, aArchive;
	/** Switch single row view / multi row view */
	private AppsAction aMulti;
	private AppsAction aToggleGridColumns;
	/** Ignore Button		*/
	public AppsAction		aIgnore;
	/** Save Button			*/
	public AppsAction		aSave;
	/** Private Lock Button	*/
	public AppsAction		aLock;
	//	Local (added to toolbar)
	@SuppressWarnings("unused")
	private AppsAction	    aReport, aEnd, aHome, aHelp, aLogout,
							aPreference,
							aOnline, aMailSupport, aAbout, aExit, 
							aDeleteSelection;
	//private AppsAction aCalculator, aCalendar, aEditor, aScript;
	
	private SwitchAction aSwitchLinesDownAction, aSwitchLinesUpAction;

	private WindowMenu m_WindowMenu;
	
	/**************************************************************************
	 *	Create Menu and Toolbar and registers keyboard actions.
	 *  - started from constructor
	 */
	private void createMenuAndToolbar()
	{
		createMenu();
		createToolBar();
		
		if (CLogMgt.isLevelAll())
		{
			Util.printActionInputMap(this);
		}
	}
	
	private void createMenu()
	{
		final IUserRolePermissions role = Env.getUserRolePermissions();
		
		/**
		 *	Menu
		 */
		//								File
		final JMenu mFile = AEnv.getMenu("File");
		menuBar.add(mFile);
		aReport = 	addAction("Report",			mFile, 	KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0),	false, false);
		aPrint = 	addAction("Print",			mFile, 	KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0),	false, false);
		aPrintPreview = addAction("PrintPreview",	mFile, KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.SHIFT_MASK+Event.ALT_MASK), false, false);
		if (role.isCanExport())
		{
			aExport = addAction("Export", mFile, null, false, false);
		}
		mFile.addSeparator();
		aEnd =	 	addAction("End",			mFile, 	KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.ALT_MASK),	false, false);
		aLogout = 	addAction("Logout", 		mFile, 	KeyStroke.getKeyStroke(KeyEvent.VK_L, Event.SHIFT_MASK+Event.ALT_MASK), false, false);
		aExit =		addAction("Exit",			mFile, 	KeyStroke.getKeyStroke(KeyEvent.VK_X, Event.SHIFT_MASK+Event.ALT_MASK),	false, false);
		//								Edit
		final JMenu mEdit = AEnv.getMenu("Edit");
		menuBar.add(mEdit);
		aNew = 		addAction("New", 			mEdit, 	KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), false, false);
		aSave = 	addAction("Save",			mEdit, 	KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0),	false, true);
		mEdit.addSeparator();
		aCopy =		addAction("Copy", 			mEdit, 	KeyStroke.getKeyStroke(KeyEvent.VK_F2, Event.SHIFT_MASK),	false, false);
		aCopyDetails = addAction("CopyDetails", mEdit, null, false, false); // metas: c.ghita@metas.ro
		aDelete = 	addAction("Delete",			mEdit, 	KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0),	false, false);
		aDeleteSelection = addAction("DeleteSelection", mEdit, KeyStroke.getKeyStroke(KeyEvent.VK_D, Event.CTRL_MASK), false, false);
		aIgnore = 	addAction(CMD_Ignore,		mEdit, 	KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),	false, false);
		aRefresh = 	addAction("Refresh",		mEdit, 	KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),	false, false);
		mEdit.addSeparator();
		aFind = 	addAction("Find",			mEdit, 	KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0), true, false);	//	toggle
		if (m_isPersonalLock)			
			aLock = addAction("Lock",			mEdit, 	null,	true, false);		//	toggle
		//								View
		final JMenu mView = AEnv.getMenu("View");
		menuBar.add(mView);
		InfoWindowMenuBuilder.newBuilder()
				.setCtx(m_ctx)
				.setParentWindowNo(getWindowNo())
				.setMenu(mView)
				.build();

		mView.addSeparator();
		aAttachment = addAction("Attachment",	mView, 	KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0),	true, false);		//	toggle
		aChat = addAction("Chat",				mView, 	null,	true, false);		//	toggle
		aHistory = 	addAction("History",		mView, 	KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0),	true, false);		//	toggle
		mView.addSeparator();
		aMulti =	addAction("Multi",			mView, 	KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),	true, false);		//	toggle
		//								Go
		final JMenu mGo = AEnv.getMenu("Go");
		menuBar.add(mGo);
		aFirst =	addAction("First", 			mGo, 	KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, Event.ALT_MASK),	false, false);
		aPrevious = addAction("Previous", 		mGo, 	KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.ALT_MASK),	false, false);
		aNext = 	addAction("Next", 			mGo, 	KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.ALT_MASK),	false, false);
		aLast =		addAction("Last",	 		mGo, 	KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, Event.ALT_MASK),	false, false);
		mGo.addSeparator();
		aParent =	addAction("Parent", 		mGo, 	KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.ALT_MASK),	false, false);
		aDetail =	addAction("Detail", 		mGo,	KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.ALT_MASK),	false, false);
		mGo.addSeparator();
		aZoomAcross = addAction("ZoomAcross",	mGo, 	null,	false, false);
		aRequest =  addAction("Request",		mGo, 	null,	false, false);
		aArchive =  addAction("Archive",		mGo, 	null,	false, false);
		aHome =		addAction("Home", 			mGo,	null,	false, false);
		//								Tools
		final JMenu mTools = AEnv.getMenu("Tools");
		menuBar.add(mTools);
		
		// metas-tsa: Drop unneeded menu items (09271)
		//@formatter:off
//		aCalculator = addAction("Calculator",	mTools, 	null,	false);
//		aCalendar = addAction("Calendar",		mTools, 	null,	false);
//		aEditor =	addAction("Editor",			mTools, 	null,	false);
//		MUser user = MUser.get(Env.getCtx());
//		if (user.isAdministrator())
//			aScript = addAction("Script",	        mTools, 	null,	false);
//		if (AEnv.isWorkflowProcess())
//			aWorkflow = addAction("WorkFlow",	mTools,		null,	false);
		//@formatter:on
		
		if ("Y".equals(Env.getContext(m_ctx, "#SysAdmin")))	//	set in DB.loginDB
			aWinSize = addAction("WinSize",     mTools, 	null,	false, false);
		AWindowSaveState.createAndAdd(this, mTools);
		if (role.isShowPreference())
		{
			mTools.addSeparator();
			aPreference = addAction("Preference",	mTools, 	null,	false, false);
		}
		
		//Window
		final AMenu aMenu = (AMenu)Env.getWindow(Env.WINDOW_MAIN);
		m_WindowMenu = new WindowMenu(aMenu.getWindowManager(), m_window);
		menuBar.add(m_WindowMenu);
		if (m_WindowMenu.isDisplayShowAllAction())
		{
			addAction(WindowMenu.ShowAllWindows_ActionName, null, WindowMenu.ShowAllWindows_KeyStroke, false, false);
		}
		
		// Help
		final JMenu mHelp = AEnv.getMenu("Help");
		menuBar.add(mHelp);
		aHelp = 	addAction("Help",			mHelp, 	KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0),	false, false);
		aOnline =	addAction("Online",			mHelp, 	null,	false, false);
		aMailSupport = addAction("EMailSupport",	mHelp,	null,	false, false);
		aAbout = 	addAction("About",			mHelp, 	null,	false, false);
	}
	
	private final void createToolBar()
	{
		// New, Save, Copy
		{
			toolBar.add(aNew.getButton());
			toolBar.add(aSave.getButton());
			toolBar.add(aCopy.getButton());
			if (CopyRecordFactory.isEnabled())
			{
				toolBar.add(aCopyDetails.getButton());							// metas: c.ghita@metas.ro
			}
		}
		
		// Ignore, Delete
		{
			addToolbarSeparator();
			toolBar.add(aIgnore.getButton());		// ESC
			toolBar.add(aDelete.getButton());
			toolBar.add(aDeleteSelection.getButton());
		}
		
		// Switch single/grid, Find, History, ZoomAccross, Refresh
		{
			addToolbarSeparator();
			if (m_curGC == null || !m_curGC.getMTab().isGridModeOnly()) // metas-2009_0021_AP1_CR059
			{
				toolBar.add(aMulti.getButton());
			}
			if (m_curGC == null || m_curGC.getMTab().isSearchActive()) // metas: Suche ausblenden?
			{
				toolBar.add(aFind.getButton());
			}
			if (!isTabIncluded)
			{
				toolBar.add(aHistory.getButton());		// F9
			}
			toolBar.add(aZoomAcross.getButton());
			toolBar.add(aRefresh.getButton());      // F5
		}
		
		// Process, Attachment, EMail, Chat
		{
			addToolbarSeparator();
			toolBar.add(AProcess.createAppsAction(this, isTabIncluded).getButton()); // metas: us1247
			toolBar.add(aAttachment.getButton());
			toolBar.add(AEMailLetter.createAppsAction(this, isTabIncluded).getButton()); // metas-2009_0017_AP1_G41
			toolBar.add(aChat.getButton());
		}

		// Reporting and printing
		{
			addToolbarSeparator();
			toolBar.add(aPrintPreview.getButton());
			toolBar.add(aPrint.getButton());
			toolBar.add(aReport.getButton());
		}

		// Archive / Request
		{
			addToolbarSeparator();
			toolBar.add(aArchive.getButton());
			toolBar.add(aRequest.getButton());
		}
		
		// Navigation (First, Previous, Next etc)
		{
			addToolbarSeparator();
			toolBar.add(aFirst.getButton());
			toolBar.add(aPrevious.getButton());
			toolBar.add(aNext.getButton());
			toolBar.add(aLast.getButton());
			if (!isTabIncluded)
			{
				toolBar.add(aParent.getButton());
				toolBar.add(aDetail.getButton());
			}
		}
		
		// Customize grid columns, Help
		{
			addToolbarSeparator();
			{
				aToggleGridColumns = AGridColumnsToggle.createAppsAction(this, isTabIncluded);
				aToggleGridColumns.setEnabled(false);
				toolBar.add(aToggleGridColumns.getButton());
			}
			toolBar.add(aHelp.getButton());			// F1
		}
	}	//	createMenu

	private final void addToolbarSeparator()
	{
		// Don't add the separator if this is the first component that we are adding it.
		if (toolBar.getComponentCount() <= 0)
		{
			return;
		}
		
		// visible line, but too narrow
		//toolBar.add(new JSeparator(JSeparator.VERTICAL));
	
		// wide separator, but no visible line
		// JToolBar.Separator separator = new JToolBar.Separator();
		// separator.setOrientation(JSeparator.VERTICAL);
		// separator.setSeparatorSize(new Dimension(50, toolBarSeparator.getHeight()));
		// toolBar.add(separator);

		// sort of works
		toolBar.addSeparator();
		final JSeparator separator = new JSeparator(JSeparator.VERTICAL);
		toolBar.add(separator);
		toolBar.addSeparator();
	}


	/**
	 * Add (Toggle) Action to Toolbar and Menu
	 * 
	 * @param actionName action name
	 * @param menu manu
	 * @param accelerator accelerator
	 * @param toggle toggle button
	 * @param alwaysRegisterAccelerator if the accelerator is specified, always register it, even if this is an included tab panel
	 * @return AppsAction
	 */
	private AppsAction addAction (final String actionName, final JMenu menu, KeyStroke accelerator, final boolean toggle, final boolean alwaysRegisterAccelerator)
	{
		final boolean isWindowMenuAction = menu != null;
		
		//
		// In case this is panel will be used for an included tab, it's better to not register the key binding
		// because that would prevent the key bindings from the main panel to work.
		if(!alwaysRegisterAccelerator && isNested && isWindowMenuAction)
		{
			accelerator = null;
		}

		//
		// Create the action/button.
		final AppsAction action = AppsAction.builder()
				.setAction(actionName)
				.setAccelerator(accelerator)
				.setToggleButton(toggle)
				.setSmallSize(isTabIncluded)
				.build();
		action.setDelegate(this);
		
		// Add the action to menu (if any)
		if (menu != null)
		{
			menu.add(action.getMenuItem());
		}

		//
		// If key binding is defined, configure the key binding at this panel level too
		if (accelerator != null)
		{
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(accelerator, actionName);
			getActionMap().put(actionName, action);
		}
		
		return action;
	}	//	addAction

	/**
	 *	Return MenuBar
	 *  @return JMenuBar
	 */
	public JMenuBar getMenuBar()
	{
		return menuBar;
	}	//	getMenuBar

	/**
	 *	Get Title of Window
	 *  @return String with Title
	 */
	public String getTitle()
	{
		if (m_mWorkbench != null && m_mWorkbench.getWindowCount() > 1)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(m_mWorkbench.getName()).append("  ")
				.append(Env.getContext(m_ctx, "#AD_User_Name")).append("@")
				.append(Env.getContext(m_ctx, "#AD_Client_Name")).append(".")
				.append(Env.getContext(m_ctx, "#AD_Org_Name")).append(" [")
				.append(Env.getContext(m_ctx, "#DB_UID")).append("]");
			return sb.toString();
		}
		return Env.getHeader(m_ctx, m_curWindowNo);
	}	//	getTitle


	private final Properties m_ctx;

	/** Workbench Model                                 */
	private GridWorkbench	m_mWorkbench;
	/** Current MTab                                    */
	private GridTab			m_curTab;
	/** Current GridController                          */
	private GridController  m_curGC;
	/** Current Window Panel                            */
	private JTabbedPane     m_curWinTab = null;
	/** Current Window No                               */
	private int				m_curWindowNo;
	/** Current Window Panel Index                      */
	private int				m_curTabIndex = -1;
	/**	Current Tab Order								*/
	private APanelTab		m_curAPanelTab = null;

	/** Dispose active                                  */
	private boolean         m_disposing = false;
	/** Save Error Message indicator                    */
	private boolean         m_errorDisplayed = false;
	/** Only current row flag                           */
	private boolean			m_onlyCurrentRows = true;
	/** Number of days to show	0=all					*/
	private int				m_onlyCurrentDays = 0;
	/** Process Info                                    */
	private boolean         m_isLocked = false;
	/** Show Personal Lock								*/
	private boolean m_isPersonalLock = Env.getUserRolePermissions().hasPermission(IUserRolePermissions.PERMISSION_PersonalLock);
	/**	Last Modifier of Action Event					*/
	private int 			m_lastModifiers;

	private final Map<Integer, GridController> includedTabId2ParentGC = new HashMap<>(4);
	private final Map<Integer, Integer> includedTabId2Height = new HashMap<>(); // metas-2009_0021_AP1_CR051
	private final int defaultIncludedTabHeight = UIManager.getInt(VPanelUI.KEY_IncludedTabHeight);
	
	/**************************************************************************
	 *	Dynamic Panel Initialization - either single window or workbench.
	 *  <pre>
	 *  either
	 *  - Workbench tabPanel    (VTabbedPane)
	 *      - Tab               (GridController)
	 *  or
	 *  - Workbench tabPanel    (VTabbedPane)
	 *      - Window            (VTabbedPane)
	 *          - Tab           (GridController)
	 *  </pre>
	 *  tabPanel
	 *  @param AD_Workbench_ID  if > 0 this is a workbench, AD_Window_ID ignored
	 *  @param AD_Window_ID     if not a workbench, Window ID
	 *  @param query			if not a Workbench, Zoom Query - additional SQL where clause
	 *  @return true if Panel is initialized successfully
	 */
	public boolean initPanel (final int AD_Workbench_ID, final int AD_Window_ID, MQuery query)
	{
		Check.assume(!isNested, "Nested panels/included tabs are not allowed tobe initialized here");
		
		log.log(Level.INFO, "WB={0}, Win={1}, Query={2}", new Object[] { AD_Workbench_ID, AD_Window_ID, query });
		
		this.setName("APanel" + AD_Window_ID);

		//  Single Window
		if (AD_Workbench_ID == 0)
			m_mWorkbench = new GridWorkbench(m_ctx, AD_Window_ID);
		else
		//  Workbench
		{
		//	m_mWorkbench = new MWorkbench(m_ctx);
		//	if (!m_mWorkbench.initWorkbench (AD_Workbench_ID))
		//	{
		//		log.log(Level.SEVERE, "APanel.initWindow - No Workbench Model");
		//		return false;
		//	}
		//	tabPanel.setWorkbench(true);
		//	tabPanel.addChangeListener(this);
			ADialog.warn(0, this, "","Not implemented yet");
			return false;
		}

		//
		// Configure Dimension
		Dimension windowSize = null;
		{
			final I_AD_Window adWindow = InterfaceWrapperHelper.create(m_ctx, AD_Window_ID, I_AD_Window.class, ITrx.TRXNAME_None);
			final int winWidth = adWindow.getWinWidth();
			final int winHeight = adWindow.getWinHeight();
			if (winWidth > 0
					|| winHeight > 0)
			{
				windowSize = new Dimension(winWidth, winHeight);
			}
		}

		if (windowSize == null)
		{
			windowSize = m_mWorkbench.getWindowSize();
		}

		MQuery detailQuery = null;
		/**
		 *  WorkBench Loop
		 */
		for (int wb = 0; wb < m_mWorkbench.getWindowCount(); wb++)
		{
			//  Get/set WindowNo
			m_curWindowNo = Env.createWindowNo (this);			                //  Timing: ca. 1.5 sec
			m_mWorkbench.setWindowNo(wb, m_curWindowNo);
			//  Set AutoCommit for this Window
			Env.setAutoCommit(m_ctx, m_curWindowNo, Env.isAutoCommit(m_ctx));
			boolean autoNew = Env.isAutoNew(m_ctx);
			Env.setAutoNew(m_ctx, m_curWindowNo, autoNew);

			//  Workbench Window
			VTabbedPane window = null;
			//  just one window
			if (m_mWorkbench.getWindowCount() == 1)
			{
				window = tabPanel;
				window.setWorkbench(false);
			}
			else
			{
				VTabbedPane tp = new VTabbedPane(false);
				window = tp;
			}
			//  Window Init
			window.addChangeListener(this);

			/**
			 *  Init Model
			 */
			int wbType = m_mWorkbench.getWindowType(wb);

			/**
			 *  Window
			 */
			if (wbType == GridWorkbench.TYPE_WINDOW)
			{
				final GridWindowVO wVO;
				try
				{
					wVO = AEnv.getMWindowVO(m_curWindowNo, m_mWorkbench.getWindowID(wb), 0);
					Check.assumeNotNull(wVO, "wVO not null");
				}
				catch (Exception ex)
				{
					// ADialog.error(0, null, "AccessTableNoView", "(No Window Model Info)");
					ADialog.error(0, this, ex);
					return false;
				}
				
				final GridWindow mWindow = new GridWindow (wVO);			                //  Timing: ca. 0.3-1 sec
				//	Set SO/AutoNew for Window
				Env.setContext(m_ctx, m_curWindowNo, "IsSOTrx", mWindow.isSOTrx());
				if (!autoNew && mWindow.isTransaction())
					Env.setAutoNew(m_ctx, m_curWindowNo, true);
				m_mWorkbench.setMWindow(wb, mWindow);
				if (wb == 0)
					m_onlyCurrentRows = mWindow.isTransaction();	//	default = only current
				if (windowSize == null)
					windowSize = mWindow.getWindowSize();

				//
				// Shall we open the window in single row layout mode?
				// * if the window is opened from zoom and the query is about one record => go single row layout
				// * else allow the window to decide (based on its settings)
				boolean goSingleRow = query != null && query.getRecordCount() == 1;

				/**
				 *  Window Tabs
				 */
				final int tabSize = mWindow.getTabCount();
				for (int tab = 0; tab < tabSize; tab++)
				{
					boolean addToWindowTabbedPane = true;
					
					//  MTab
					if (tab == 0) mWindow.initTab(0);
					final GridTab gTab = mWindow.getTab(tab);
					Env.setContext(m_ctx, m_curWindowNo, tab, GridTab.CTX_TabLevel, Integer.toString(gTab.getTabLevel()));
					//  Query first tab
					if (tab == 0)
					{
						//  initial user query for single workbench tab
						if (m_mWorkbench.getWindowCount() == 1)
						{
							//
							// If we have a valid zoom query but is not for current Tab's table
							// then we will use an "no record query" because we will refresh it later
							if (query != null && query.getZoomTableName() != null && query.getZoomColumnName() != null
								&& query.getZoomValue() instanceof Integer && (Integer)query.getZoomValue() > 0)
					    	{
					    		if (!query.getZoomTableName().equalsIgnoreCase(gTab.getTableName()))
					    		{
					    			detailQuery = query;
					    			query = MQuery.getNoRecordQuery(gTab.getTableName(), true);
					    			// NOTE: make sure RecordCount is zero, else initialQuery() method will trigger the find panel to popup  
					    			query.setRecordCount(0);
					    		}
					    	}
							
							isCancel = false; //Goodwill
							query = initialQuery (query, gTab);
							if (isCancel) return false; //Cancel opening window
							
							if (query != null && query.getRecordCount() <= 1
									&& !query.toString().equals("1=2")) // metas: query.toString().equals("1=2") um Fenster die ohne queryOnLoad geladen werden in Zeilenansicht zu oeffnen
							{
								goSingleRow = true;
							}
						}
						else if (wb != 0)
						//  workbench dynamic query for dependent windows
						{
							query = m_mWorkbench.getQuery();
						}
						// Set initial Query on first tab (metas: if the query has the right table!)
						if (query != null /* && query.getZoomTableName().equals(gTab.getTableName())*/)  // metas: commented that condition
						{
							m_onlyCurrentRows = false;  //  Query might involve history
							gTab.setQuery(query);
						}
						if (wb == 0)
							m_curTab = gTab;
					}	//	query on first tab

					final Component tabElement;
					//  GridController
					if (gTab.isSortTab())
					{
						final VSortTab st = new VSortTab(m_curWindowNo, gTab.getAD_Table_ID(), gTab.getAD_ColumnSortOrder_ID(), gTab.getAD_ColumnSortYesNo_ID());
						st.setTabLevel(gTab.getTabLevel());
						tabElement = st;
					}
					else	//	normal tab
					{
						//
						// Check if this is an included tab (metas-2009_0021_AP1_CR056)
						boolean includedTab = false;
						if (includedTabId2ParentGC.size() > 0)
						{
							if (includedTabId2ParentGC.get(gTab.getAD_Tab_ID()) != null)
								includedTab = true;
						}
						
						final GridController gc = GridController.builder()
								.setAPanel(this)
								.setGridTab(gTab)
								.setIncludedTab(includedTab)
								// If we have a zoom query, switch to single row
								.setGoSingleRowLayout(tab == 0 && goSingleRow)
								.build();
						
						//	Set First Tab
						if (wb == 0 && tab == 0)
						{
							m_curGC = gc;
						}
						
						tabElement = gc;
						
                        // FR [ 1757088 ]
						for (final GridField gridField : gTab.getFields())
						{
							final int includedTabId = gridField.getIncluded_Tab_ID();
							if (includedTabId > 0)
							{
								includedTabId2ParentGC.put(includedTabId, gc);
								includedTabId2Height.put(includedTabId, gridField.getIncludedTabHeight()); // metas-2009_0021_AP1_CR051
							}
						}

						//	Is this tab included?
						if (includedTabId2ParentGC.size() > 0)
						{
							final GridController parentGC = includedTabId2ParentGC.get(gTab.getAD_Tab_ID());
							if (parentGC != null)
							{
								// 
								// Set included tab to a fixed height
								final Integer height = includedTabId2Height.get(gTab.getAD_Tab_ID());
								gc.setFixedHeight(height == null || height <= 0 ? defaultIncludedTabHeight : height.intValue());
								
								// FR [ 1757088 ]
								gc.removeDataStatusListener(this);
								isTabIncluded = true; // metas-2009_0021_AP1_CR056
								final GridSynchronizer synchronizer = new GridSynchronizer(mWindow, parentGC, gc);
								if (parentGC == m_curGC)
									synchronizer.activateChild();
								parentGC.includeTab(synchronizer);
								
								addToWindowTabbedPane = false;
							}
						}
						
						initSwitchLineAction();
					}	//	normal tab

					// Add the tab element to TabbedPane
					if (addToWindowTabbedPane)
					{
						// Add Tab - sets ALT-<number> and Shift-ALT-<x>
						final String tabTitle = buildTabTitle(gTab);
						window.addTab(tabTitle, gTab, tabElement);
					}
				}   //  Tab Loop
			}   //  Type-MWindow

			//  Single Workbench Window Tab
			if (m_mWorkbench.getWindowCount() == 1)
			{
				window.setToolTipText(m_mWorkbench.getDescription(wb));
			}
			else
			//  Add Workbench Window Tab
			{
				tabPanel.addTab(m_mWorkbench.getName(wb), m_mWorkbench.getIcon(wb), window, m_mWorkbench.getDescription(wb));
			}
			//  Used for Env.getHeader
			Env.setContext(m_ctx, m_curWindowNo, "WindowName", m_mWorkbench.getName(wb));

		}   //  Workbench Loop

		//  stateChanged (<->) triggered
		toolBar.setName(getTitle());
		m_curTab.getTableModel().setChanged(false);
		//	Set Detail Button
		aDetail.setEnabled(0 != m_curWinTab.getTabCount()-1);

		//	Enable/Disable Tabs dynamically
		if (m_curWinTab instanceof VTabbedPane)
			((VTabbedPane)m_curWinTab).evaluate(null);
		//	Size
		if (windowSize != null)
			setPreferredSize(windowSize);
		else
			revalidate();

		if (detailQuery != null && zoomToDetailTab(detailQuery)) {
			return true;
		}

		Dimension size = getPreferredSize();
		log.info( "fini - " + size);
		m_curWinTab.requestFocusInWindow();
		return true;
	}	//	initPanel
	
	private static final String buildTabTitle(final GridTab gridTab)
	{
		final StringBuilder tabTitle = new StringBuilder();
		tabTitle.append ("<html>");
		if (gridTab.isReadOnly())
			tabTitle.append("<i>");
		int pos = gridTab.getName ().indexOf (" ");
		if (pos == -1)
			tabTitle.append (gridTab.getName ()).append ("<br>&nbsp;");
		else
		{
			tabTitle.append (gridTab.getName().substring (0, pos))
			  .append ("<br>")
			  .append (gridTab.getName().substring(pos + 1));
		}
		if (gridTab.isReadOnly())
			tabTitle.append("</i>");
		tabTitle.append ("</html>");

		return tabTitle.toString();
	}
	
	private boolean zoomToDetailTab(MQuery query)
	{
		if (query != null 
				&& query.getZoomTableName() != null
				&& query.getZoomColumnName() != null)
    	{
    		GridTab gTab = m_mWorkbench.getMWindow(0).getTab(0);
    		if (!query.getZoomTableName().equalsIgnoreCase(gTab.getTableName()))
    		{
    			int tabSize = m_mWorkbench.getMWindow(0).getTabCount();

    	        for (int tabNo = 0; tabNo < tabSize; tabNo++)
    	        {
    	        	gTab = m_mWorkbench.getMWindow(0).getTab(tabNo);
    	        	if (gTab.isSortTab())
    	        		continue;

    	        	if (gTab.getTableName().equalsIgnoreCase(query.getZoomTableName()))
    	        	{
	        			if (doZoomToDetail(gTab, query, tabNo)) {
	        				return true;
	        			}
    	        	}
    	        }
    		}
    	}
        return false;
	}

	private boolean doZoomToDetail(final GridTab gTab, final MQuery query, final int tabIndex)
	{
		final GridField[] fields = gTab.getFields();
		for (GridField field : fields)
		{
			if (field.getColumnName().equalsIgnoreCase(query.getZoomColumnName()))
			{
				m_mWorkbench.getMWindow(0).initTab(tabIndex);
				int parentId = DB.getSQLValue(null, "SELECT " + gTab.getLinkColumnName() + " FROM " + gTab.getTableName() + " WHERE " + query.getWhereClause());
				if (parentId > 0)
				{
					Map<Integer, Object[]>parentMap = new TreeMap<Integer, Object[]>();
					int index = tabIndex;
					int oldpid = parentId;
					GridTab currentTab = gTab;
					while (index > 0)
					{
						index--;
						GridTab pTab = m_mWorkbench.getMWindow(0).getTab(index);
						if (pTab.getTabLevel() < currentTab.getTabLevel())
						{
							m_mWorkbench.getMWindow(0).initTab(index);
							if (index > 0)
							{
								if (pTab.getLinkColumnName() != null && pTab.getLinkColumnName().trim().length() > 0)
								{
									int pid = DB.getSQLValue(null, "SELECT " + pTab.getLinkColumnName() + " FROM " + pTab.getTableName() + " WHERE " + currentTab.getLinkColumnName() + " = ?", oldpid);
									if (pid > 0)
									{
										parentMap.put(index, new Object[]{currentTab.getLinkColumnName(), oldpid});
										oldpid = pid;
										currentTab = pTab;
									}
									else
									{
										parentMap.clear();
										break;
									}
								}
							}
							else
							{
								parentMap.put(index, new Object[]{currentTab.getLinkColumnName(), oldpid});
							}
						}
					}
					for(Map.Entry<Integer, Object[]> entry : parentMap.entrySet())
					{
						GridTab pTab = m_mWorkbench.getMWindow(0).getTab(entry.getKey());
						Object[] value = entry.getValue();
						MQuery pquery = new MQuery(pTab.getAD_Table_ID());
						pquery.addRestriction((String)value[0], "=", value[1]);
						pTab.setQuery(pquery);
						GridController gc = (GridController) tabPanel.getComponentAt(entry.getKey());
						gc.activate();
						gc.query(false, 0, GridTabMaxRows.NO_RESTRICTION);
					}


					MQuery targetQuery = new MQuery(gTab.getAD_Table_ID());
					targetQuery.addRestriction(gTab.getLinkColumnName(), "=", parentId);
					gTab.setQuery(targetQuery);
					GridController gc = null;
					if (!includedTabId2ParentGC.containsKey(gTab.getAD_Tab_ID()))
					{
						int target = tabPanel.findTabindex(gTab);
						gc = (GridController) tabPanel.getComponentAt(target);
					}
					else
					{
						GridController parent = includedTabId2ParentGC.get(gTab.getAD_Tab_ID());
						gc = parent.findChild(gTab);
					}
					gc.activate();
					gc.query(false, 0, GridTabMaxRows.NO_RESTRICTION);

					GridTable table = gTab.getTableModel();
    				int count = table.getRowCount();
    				for(int i = 0; i < count; i++)
    				{
    					int id = table.getKeyID(i);
    					if (id == ((Integer)query.getZoomValue()).intValue())
    					{
    						if (!includedTabId2ParentGC.containsKey(gTab.getAD_Tab_ID()))
    						{
    							tabPanel.setSelectedIndex(tabPanel.findTabindex(gTab));
    						}
    						else
    						{
    							GridController parent = includedTabId2ParentGC.get(gTab.getAD_Tab_ID());
    							int pindex = tabPanel.findTabindex(parent.getMTab());
    							if (pindex >= 0)
    								tabPanel.setSelectedIndex(pindex);
    						}
    						gTab.navigate(i);
    						return true;
    					}
    				}
				}
			}
		}
		return false;
	}

	/**
	 * 	Get Current Window No
	 *	@return win no
	 */
	public int getWindowNo()
	{
		return m_curWindowNo;
	}	//	getWindowNo
	
	/**
	 * Get current TabNo
	 * @return TabNo or {@link Env#TAB_None}
	 */
	public int getTabNo()
	{
		if (m_curTab == null)
		{
			return Env.TAB_None;
		}
		return m_curTab.getTabNo();
	}
	
	/**
	 * 	Initial Query
	 *	@param query initial query
	 *	@param mTab tab
	 *	@return query or null
	 */
	private MQuery initialQuery (MQuery query, final GridTab mTab)
	{
		// We have a (Zoom) query
		if (query != null && query.isActive())
		{
			final GridTabMaxRowsRestrictionChecker maxRowsChecker = GridTabMaxRowsRestrictionChecker.builder()
					.setUserRolePermissions(Env.getUserRolePermissions(m_ctx))
					.setAD_Tab(mTab)
					.build();
			if (!maxRowsChecker.isQueryMax(query.getRecordCount()))
			{
				return query;
			}
		}
		
		//
		final StringBuilder where = new StringBuilder(Env.parseContext(m_ctx, m_curWindowNo, mTab.getWhereExtended(), false));
		//	Query automatically if high volume and no query
		boolean require = mTab.isHighVolume();
		
		// metas-2009_0021_AP1_CR064: begin
		if (!mTab.isQueryOnLoad())
		{
			return MQuery.getNoRecordQuery(mTab.getTableName(), false);
		}
		// metas-2009_0021_AP1_CR064: end
		
		if (!require
				&& !m_onlyCurrentRows // No Trx Window
				&& mTab.isQueryOnLoad()) // metas-2009_0021_AP1_CR064
		{
			/*  Where Extended already appended above, check for variables */
			if (query != null)
			{
				final String wh2 = query.getWhereClause();
				if (wh2.length() > 0)
				{
					if (where.length() > 0)
						where.append (" AND ");
					where.append(wh2);
				}
			}
			//
			final StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ")
				.append(mTab.getTableName());
			if (where.length() > 0)
				sql.append(" WHERE ").append(where);
			//	Does not consider security
			final int no = DB.getSQLValue(ITrx.TRXNAME_None, sql.toString());
			//
			require = GridTabMaxRowsRestrictionChecker.builder()
					.setAD_Tab(mTab)
					.build()
					.isQueryRequire(no);
		}
		//	Show Query
		if (require)
		{
			Find find = Find.builder()
					.setParentFrame(getCurrentFrame())
					.setTargetWindowNo(m_curWindowNo)
					.setGridTab(mTab)
					.setFindFields(mTab.getFields())
					.setWhereExtended(where.toString())
					.setQuery(query)
					.setMinRecords(10) // no query below 10
					.buildFindDialog();
			query = find.getQuery();
			query = find.isCancel() ? null : query; // metas: teo_sarca: metas-2009_0021_AP1_G113
			isCancel = (query == null);//Goodwill
			find.dispose();
			find = null;
		}
		return query;
	}	//	initialQuery
	
	
	/**
	 *  Get Window Index
	 *  @return Window Index
	 */
	private int getWindowIndex()
	{
		//  only one window
		if (m_mWorkbench.getWindowCount() == 1)
			return 0;
		//  workbench
		return tabPanel.getSelectedIndex();
	}   //  getWindowIndex

	/**
	 *  Is first Tab (on Window)
	 *  @return true if the panel displays the first tab
	 */
	private boolean isFirstTab()
	{
		return m_curWinTab.getSelectedIndex() == 0;
	}   //  isFirstTab

	/**
	 * 	Get Window Image
	 *	@return image or null
	 */
	public Image getImage()
	{
		return m_mWorkbench.getImage(getWindowIndex());
	}	//	getImage
	
	
	/**************************************************************************
	 *	Data Status Listener (row change)			^ | v
	 *  @param e event
	 */
	@Override
	public void dataStatusChanged (final DataStatusEvent e)
	{
		if (m_disposing)
		{
			return;
		}
		
		if(log.isLoggable(Level.INFO))
			log.info(e.getMessage());
		
		String dbInfo = e.getMessage();
		if (m_curTab != null && m_curTab.isQueryActive())
			dbInfo = "[ " + dbInfo + " ]";
		statusBar.setStatusDB(dbInfo, e);
		if (!isNested && m_window != null)
			m_window.setTitle(getTitle());

		//	Set Message / Info
		if (e.getAD_Message() != null || e.getInfo() != null)
		{
			StringBuilder sb = new StringBuilder();
			String msg = e.getMessage();
			if (msg != null && msg.length() > 0)
				sb.append(Services.get(IMsgBL.class).getMsg(m_ctx, e.getAD_Message()));
			String info = e.getInfo();
			if (info != null && info.length() > 0)
			{
				if (sb.length() > 0 && !sb.toString().trim().endsWith(":"))
					sb.append(": ");
				sb.append(info);
			}
			if (sb.length() > 0)
			{
				int pos = sb.indexOf("\n");
				if (pos != -1)  // replace CR/NL
					sb.replace(pos, pos+1, " - ");
				setStatusLine (sb.toString (), e.isError ());
			}
		}

		//  Confirm Error
		if (e.isError() && !e.isConfirmed())
		{
			ADialog.error(m_curWindowNo, this, e.getAD_Message(), e.getInfo());
			e.setConfirmed(true);   //  show just once - if MTable.setCurrentRow is involved the status event is re-issued
			m_errorDisplayed = true;
		}
		//  Confirm Warning
		else if (e.isWarning() && !e.isConfirmed())
		{
			ADialog.warn(m_curWindowNo, this, e.getAD_Message(), e.getInfo());
			e.setConfirmed(true);   //  show just once - if MTable.setCurrentRow is involved the status event is re-issued
		}

		//	update Navigation
		boolean firstRow = e.isFirstRow();
		aFirst.setEnabled(!firstRow);
		aPrevious.setEnabled(!firstRow);
		boolean lastRow = e.isLastRow();
		aNext.setEnabled(!lastRow);
		aLast.setEnabled(!lastRow);

		//	update Change
		
		boolean changed = e.isChanged() || e.isInserting();
		int changedColumn = e.getChangedColumn();
		boolean inserting = e.isInserting();
		
		if(e.getAD_Message() != null && e.getAD_Message().equals("Saved"))
			changed = false;
		boolean readOnly = m_curTab.isReadOnly();
		final boolean insertRecord = m_curTab.isInsertRecord();
		final boolean deleteRecord = m_curTab.isDeletable();

		aNew.setEnabled(((inserting && changedColumn>0) || !inserting) && insertRecord);
		aCopy.setEnabled(!changed && insertRecord);
		aCopyDetails.setEnabled(!changed && insertRecord && CopyRecordFactory.isEnabled(m_curTab)); 
		aRefresh.setEnabled(!changed);
		aDelete.setEnabled(!changed && deleteRecord);
		aDeleteSelection.setEnabled(!changed && deleteRecord); // same as "aDelete"
		//
		if (readOnly && m_curTab.isAlwaysUpdateField())
			readOnly = false;
		aIgnore.setEnabled(changed && !readOnly);
		aSave.setEnabled(changed && !readOnly);
		//
		//	No Rows
		if (e.getTotalRows() == 0 && insertRecord) 
		{
			aNew.setEnabled(true);
			aDelete.setEnabled(false);
			aDeleteSelection.setEnabled(false);
		}

		//	Single-Multi
		aMulti.setPressed(!m_curGC.isSingleRow());
		aToggleGridColumns.setEnabled(!m_curGC.isSingleRow());

		//	History	(on first Tab only)
		if (isFirstTab())
			aHistory.setPressed(!m_curTab.isOnlyCurrentRows());

		//	Summary info
		final String summaryInfoMessage = m_curTab.getSummaryInfoMessage();
		if (summaryInfoMessage != null)
		{
			statusBar.setInfo(summaryInfoMessage);
		}

		//	Check Attachment
		boolean canHaveAttachment = m_curTab.canHaveAttachment();		//	not single _ID column
		//
		if (canHaveAttachment && e.isLoading() && m_curTab.getCurrentRow() > e.getLoadedRows())
			canHaveAttachment = false;
		if (canHaveAttachment && m_curTab.getRecord_ID() == -1)    //	No Key
			canHaveAttachment = false;
		if (canHaveAttachment)
		{
			aAttachment.setEnabled(true);
			aAttachment.setPressed(m_curTab.hasAttachment());
			aChat.setEnabled(true);
			aChat.setPressed(m_curTab.hasChat());
		}
		else
		{
			aAttachment.setEnabled(false);
			aChat.setEnabled(false);
		}
		//	Lock Indicator
		if (m_isPersonalLock)
			aLock.setPressed(m_curTab.isLocked());

		if (m_curWinTab instanceof VTabbedPane)
		{
			((VTabbedPane)m_curWinTab).evaluate(e);
		}
	}	//	dataStatusChanged

	/**
	 *	Set Status Line to text
	 *  @param text clear text
	 *  @param error error flag
	 */
	public void setStatusLine (String text, boolean error)
	{
		log.fine(text);
		statusBar.setStatusLine(text, error);
	}	//	setStatusLine

	/**
	 *	Indicate Busy
	 *  @param busy busy
	 *  @param focus request focus (only if <code>busy</code> is <code>false</code>)
	 */
	private final void setBusy (final boolean busy, final boolean focus)
	{
		m_isLocked = busy;
		//
		JFrame frame = getCurrentFrame();
		if (frame == null)  //  during init
			return;
		if (frame instanceof AWindow)
			((AWindow)frame).setBusy(busy);
	//	String processing = Services.get(IMsgBL.class).getMsg(m_ctx, "Processing");
		if (busy)
		{
	//		setStatusLine(processing);
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
		else
		{
			this.setCursor(Cursor.getDefaultCursor());
			frame.setCursor(Cursor.getDefaultCursor());
			if (focus)
			{
				m_curGC.requestFocus();
			}
			
	//		if (statusBar.getStatusLine().equals(processing))
	//			statusBar.setStatusLine("");
		}
	}	//	set Busy

	
	/**************************************************************************
	 *	Change Listener - (tab change)			<->
	 *  @param e event
	 */
	@Override
	public final void stateChanged (final ChangeEvent e)
	{
		if (m_disposing)
		{
			return;
		}
		
		log.info(e.toString());
		
		setBusy(true, true); // busy=true, focus=N/A
		
		try
		{
			stateChanged0(e);
		}
		finally
		{
			setBusy(false, true); // busy=false, focus=true
		}
	}
	
	private final void stateChanged0(final ChangeEvent e)
	{
		final VTabbedPane tp = (VTabbedPane)e.getSource();
		boolean back = false; // true if user navigated from a bottom tab to a top tab
		boolean isAPanelTab = false;

		//int previousIndex = 0;

		//  Workbench Tab Change
		if (tp.isWorkbench())
		{
			int WBIndex = tabPanel.getSelectedIndex();
			m_curWindowNo = m_mWorkbench.getWindowNo(WBIndex);
			//  Window Change
			log.info("curWin=" + m_curWindowNo + " - Win=" + tp);
			if (tp.getSelectedComponent() instanceof JTabbedPane)
				m_curWinTab = (JTabbedPane)tp.getSelectedComponent();
			else
				throw new java.lang.IllegalArgumentException("Window does not contain Tabs");
			if (m_curWinTab.getSelectedComponent() instanceof GridController)
			{
				m_curGC = (GridController)m_curWinTab.getSelectedComponent();
				initSwitchLineAction();
			}
		//	else if (m_curWinTab.getSelectedComponent() instanceof APanelTab)
		//		isAPanelTab = true;
			else
				throw new java.lang.IllegalArgumentException("Window-Tab does not contain GridControler");
			//  change pointers
			m_curTabIndex = m_curWinTab.getSelectedIndex();
		}
		else
		{
			//  Just a Tab Change
			log.info("Tab=" + tp);
			m_curWinTab = tp;
			int tpIndex = m_curWinTab.getSelectedIndex();
			//	detect no tab change
			if (tpIndex == m_curTabIndex)
			{
				return;
			}
			
			back = tpIndex < m_curTabIndex;
			GridController gc = null;
			if (m_curWinTab.getSelectedComponent() instanceof GridController)
				gc = (GridController)m_curWinTab.getSelectedComponent();
			else if (m_curWinTab.getSelectedComponent() instanceof APanelTab)
				isAPanelTab = true;
			else
				throw new java.lang.IllegalArgumentException("Tab does not contain GridControler");
			//  Save old Tab
			if (m_curGC != null)
			{
				m_curGC.stopEditor(true);
				//  has anything changed?
				if (m_curTab.needSave(true, false))
				{   //  do we have real change
					if (m_curTab.needSave(true, true))
					{
						//	Automatic Save
						if (Env.isAutoCommit(m_ctx, m_curWindowNo))
						{
							if (!m_curTab.dataSave(true))
							{	//  there is a problem, so we go back
								showLastError();
								m_curWinTab.setSelectedIndex(m_curTabIndex);
								return;
							}
						}
						//  explicitly ask when changing tabs
						else if (ADialog.ask(m_curWindowNo, this, "SaveChanges?", m_curTab.getCommitWarning()))
						{   //  yes we want to save
							if (!m_curTab.dataSave(true))
							{   //  there is a problem, so we go back
								showLastError();
								m_curWinTab.setSelectedIndex(m_curTabIndex);
								return;
							}
						}
						else    //  Don't save
						{
							int newRecord = m_curTab.getTableModel().getNewRow(); // VOSS COM
							if (newRecord == -1)
								m_curTab.dataIgnore();
							else
							{
								m_curWinTab.setSelectedIndex(m_curTabIndex);
								return;
							}
						}
					}
					else    //  new record, but nothing changed
						m_curTab.dataIgnore();
				}   //  there is a change
			}
			if (m_curAPanelTab != null)
			{
				m_curAPanelTab.saveData();
				m_curAPanelTab.unregisterPanel();
				m_curAPanelTab = null;
			}

			//	new tab
		//	if (m_curTabIndex >= 0)
		//		m_curWinTab.setForegroundAt(m_curTabIndex, AdempierePLAF.getTextColor_Normal());
		//	m_curWinTab.setForegroundAt(tpIndex, AdempierePLAF.getTextColor_OK());
		//	previousIndex = m_curTabIndex;
			m_curTabIndex = tpIndex;
			if (!isAPanelTab)
			{
				m_curGC = gc;
				initSwitchLineAction();
			}
		}

		//	Sort Tab Handling
		if (isAPanelTab)
		{
			m_curAPanelTab = (APanelTab)m_curWinTab.getSelectedComponent();
			m_curAPanelTab.registerAPanel(this);
			m_curAPanelTab.loadData();
			// Consider that APanelTab (e.g. VSortTab) is not navigable - teo_sarca [ 1705444 ]
			aFirst.setEnabled(false);
			aPrevious.setEnabled(false);
			aNext.setEnabled(false);
			aLast.setEnabled(false);
		}
		else	//	Cur Tab Setting
		{
			int gwTabIndex = m_mWorkbench.getMWindow(0).getTabIndex(m_curGC.getMTab());
			//boolean needValidate = false;
			if (m_mWorkbench.getMWindow(0).isTabInitialized(gwTabIndex) == false)
			{
				m_mWorkbench.getMWindow(0).initTab(gwTabIndex);
				//needValidate = true;
			}
			m_curGC.activate();
			m_curTab = m_curGC.getMTab();
			isSearchActive = m_curTab.isSearchActive(); // metas-2009_0021_AP1_CR057
			// metas-2009_0021_AP1_CR050: begin
			if (back && m_curTab.isRefreshAllOnActivate())
			{
				// 06984 start: fix refresh for view-inactive-tabs (i.e index out of bounds exception / or no refresh triggerred)
				m_curGC.query(m_onlyCurrentRows, m_onlyCurrentDays, GridTabMaxRows.DEFAULT);

				m_curTab.dataRefreshAll(); // Note: this is still needed because query() does not always trigger it and you might end up with an IndexOutOfBoundsException
				// 06984 end
			}
			else if (back && m_curTab.isCurrent()) // Note when refreshing a subtab WITH DISPLAY LOGIC, this is actually the parent tab
			{
				// metas-2009_0021_AP1_CR050: end
				// Refresh only current row when tab is current
				m_curTab.dataRefresh();
			}
			else
			{
				// Requery & autoSize
				m_curGC.query(m_onlyCurrentRows, m_onlyCurrentDays, GridTabMaxRows.DEFAULT);
				// @formatter:off
				/*
				if (m_curGC.isNeedToSaveParent())
				{
					// there is a problem, so we go back
					ADialog.error(m_curWindowNo, this, "SaveParentFirst");
					m_curWinTab.setSelectedIndex(previousIndex);
					setBusy(false, true);
					return;
				} */
				// @formatter:on
			}
			//  Set initial record
			if (m_curTab.getRowCount() == 0)
			{
				//	Automatically create New Record, if none & tab not RO
				if (m_curTab.isInsertRecord() 
					&& (Env.isAutoNew(m_ctx, m_curWindowNo) || m_curTab.isQueryNewRecord()))
				{
					log.config("No record - New - AutoNew=" + Env.isAutoNew(m_ctx, m_curWindowNo)
						+ " - QueryNew=" + m_curTab.isQueryNewRecord());
					m_curTab.dataNew(DataNewCopyMode.NoCopy);
				}
				else	//	No Records found
				{
					aSave.setEnabled(false);
					aDelete.setEnabled(false);
					aDeleteSelection.setEnabled(false);
				}
				m_curTab.navigateCurrent();     //  updates counter
				m_curGC.dynamicDisplay(0);
			}
			/*
			if (needValidate)
			{
				JFrame frame = Env.getFrame(APanel.this);
				if (frame != null)
				{
					//not sure why, the following lines is needed to make dynamic resize work
					//tested on jdk1.5, 1.6 using jgoodies look and feel
					frame.getPreferredSize();
					
					if (frame.getExtendedState() != JFrame.MAXIMIZED_BOTH)
					{
						frame.setMinimumSize(frame.getSize());
						revalidate();
						SwingUtilities.invokeLater(new Runnable() {
		
							public void run() {
								JFrame frame = Env.getFrame(APanel.this);
								frame.validate();
								AEnv.showCenterScreen(frame);
								frame.setMinimumSize(null);
							}
							
						});
					}
				}
			}*/
		//	else		##CHANGE
		//		m_curTab.navigateCurrent();
		}

		//	Update <-> Navigation
		aDetail.setEnabled(m_curTabIndex != m_curWinTab.getTabCount()-1);
		aParent.setEnabled(m_curTabIndex != 0 && m_curWinTab.getTabCount() > 1);

		//	History (on first tab only)
		if (m_mWorkbench.getMWindow(getWindowIndex()).isTransaction())
			aHistory.setEnabled(isFirstTab());
		else
		{
			aHistory.setPressed(false);
			aHistory.setEnabled(false);
		}
		//	Document Print
		aPrint.setEnabled(m_curTab.isPrinted());
		aPrintPreview.setEnabled(m_curTab.isPrinted());
		//	Query
		aFind.setPressed(m_curTab.isQueryActive());

		//	Order Tab
		if (isAPanelTab)
		{
			aMulti.setPressed(false);
			aMulti.setEnabled(false);
			aToggleGridColumns.setEnabled(false);
			aNew.setEnabled(false);
			aDelete.setEnabled(false);
			aDeleteSelection.setEnabled(false);
			aFind.setEnabled(false);
			aRefresh.setEnabled(false);
			aAttachment.setEnabled(false);
			aChat.setEnabled(false);
		}
		else	//	Grid Tab
		{
			aMulti.setEnabled(true);
			aMulti.setPressed(!m_curGC.isSingleRow());
			// metas-2009_0021_AP1_CR057: begin
			aFind.setEnabled(m_curGC.getMTab().isSearchActive()); //metas Suche aktiv in included Tab?
			if (!isSearchActive)
			{
				toolBar.remove(aFind.getButton());
			}
			// metas-2009_0021_AP1_CR057: end
			aMulti.setEnabled(!m_curGC.getMTab().isGridModeOnly()); //metas-2009_0021_AP1_CR059
			aToggleGridColumns.setEnabled(!m_curGC.isSingleRow());
			aRefresh.setEnabled(true);
			aAttachment.setEnabled(true);
			aChat.setEnabled(true);
		}
		
		//
		m_curWinTab.requestFocusInWindow();
		
		log.config( "fini");
	}	//	stateChanged

	/**
	 *	Navigate to Detail Tab			->
	 */
	private void cmd_detail()
	{
		int index = m_curWinTab.getSelectedIndex();
		if (index == m_curWinTab.getTabCount()-1)
			return;
		//hengsin, bug [ 1637763 ]
		if (m_curWinTab instanceof VTabbedPane)
		{
			VTabbedPane tabPane = (VTabbedPane)m_curWinTab;
			index++;
			while ( index < tabPane.getTabCount() )
			{
				if (tabPane.isEnabledAt(index))
				{
					m_curGC.getTable().removeEditor();
					m_curGC.acceptEditorChanges();
					tabPane.setSelectedIndex(index);
					break;
				}
				else
					index++;
			}
		}
		else
		{
			m_curGC.getTable().removeEditor();
			m_curGC.acceptEditorChanges();
			m_curWinTab.setSelectedIndex(index+1);
		}
			
	}	//	navigateDetail

	/**
	 *	Navigate to Parent Tab			<-
	 */
	private void cmd_parent()
	{
		int index = m_curWinTab.getSelectedIndex();
		if (index == 0)
			return;
		//hengsin, bug [ 1637763 ]
		if (m_curWinTab instanceof VTabbedPane)
		{
			VTabbedPane tabPane = (VTabbedPane)m_curWinTab;
			index--;
			while ( index >= 0 )
			{
				if (tabPane.isEnabledAt(index))
				{
					m_curGC.getTable().removeEditor();
					m_curGC.acceptEditorChanges();
					tabPane.setSelectedIndex(index);
					break;
				}
				else
					index--;
			}
		}
		else
		{
			m_curGC.getTable().removeEditor();
			m_curGC.acceptEditorChanges();
			m_curWinTab.setSelectedIndex(index-1);
		}
	}	//	navigateParent

	
	/**************************************************************************
	 *	Action Listener
	 *  @param e event
	 */
	@Override
	public void actionPerformed (ActionEvent e)
	{
		if(log.isLoggable(Level.INFO))
			log.info(e.getActionCommand() + " - " + e.getModifiers());
		
		if (m_disposing || isUILocked())
			return;
			
		m_lastModifiers = e.getModifiers();
		String cmd = e.getActionCommand();

		//  Problem: doubleClick detection - can't disable button as clicking button may change button status
		if (!WindowMenu.ShowAllWindows_ActionName.equals(cmd))
		{
			setBusy (true, true);
		}
		
		boolean requestFocus = true;
		try
		{
			//  Command Buttons
			if (e.getSource() instanceof VButton)
			{
				setStatusLine(processButtonCallout((VButton)e.getSource()), true);
				actionButton((VButton)e.getSource());
				return;
			}
			
			//	File
			if (cmd.equals(aReport.getName()))
				cmd_report();
			else if (cmd.equals(aPrint.getName()))
				cmd_print();
			else if (cmd.equals(aPrintPreview.getName()))
				cmd_print(true);
			else if (aExport != null && cmd.equals(aExport.getName()))
				cmd_export();
			else if (cmd.equals(aEnd.getName()))
				cmd_end(false);
			else if (cmd.equals(aExit.getName()))
				cmd_end(true);
			//	Edit
			else if (cmd.equals(aNew.getName()))
				cmd_new(DataNewCopyMode.NoCopy);
			else if (cmd.equals(aSave.getName()))
				cmd_save(true);
			else if (cmd.equals(aCopy.getName()))
				cmd_new(DataNewCopyMode.Copy);
			else if (cmd.equals(aCopyDetails.getName())) // metas: c.ghita@metas.ro
				cmd_new(DataNewCopyMode.CopyWithDetails);
			else if (cmd.equals(aDelete.getName()))
				cmd_delete();
			else if (cmd.equals(aDeleteSelection.getName()))
				cmd_deleteSelection();
			else if (cmd.equals(aIgnore.getName()))
				cmd_ignore();
			else if (cmd.equals(aRefresh.getName()))
				cmd_refresh();
			else if (cmd.equals(aFind.getName()))
				cmd_find();
			else if (m_isPersonalLock && cmd.equals(aLock.getName()))
				cmd_lock();
			//	View
			else if (cmd.equals(aAttachment.getName()))
				cmd_attachment();
			else if (cmd.equals(aChat.getName()))
				cmd_chat();
			else if (cmd.equals(aHistory.getName()))
				cmd_history();
			else if (cmd.equals(aMulti.getName()))
			{
				m_curGC.switchRowPresentation();
				aToggleGridColumns.setEnabled(m_curGC != null && !m_curGC.isSingleRow());
			}
			//	Go
			else if (cmd.equals(aHome.getName()))
			{
				cmd_home();
				requestFocus = false; // to not let subsequent code to show this window back
			}
			else if (cmd.equals(aFirst.getName()))
			{	/*cmd_save(false);*/
				m_curGC.getTable().removeEditor();
				m_curGC.acceptEditorChanges();
				m_curTab.navigate(0);
			}
			else if (cmd.equals(aSwitchLinesUpAction.getName())) 
			{
				//up-key + shift
				m_curGC.getTable().removeEditor();
				m_curTab.switchRows(m_curTab.getCurrentRow(), m_curTab.getCurrentRow() - 1, m_curGC.getTable().getSortColumn(), m_curGC.getTable().isSortAscending());
				m_curGC.getTable().requestFocus();
			} 
			else if (cmd.equals(aPrevious.getName())) 
			{ /* cmd_save(false); */
				//up-image + shift
				m_curGC.getTable().removeEditor();
				m_curGC.acceptEditorChanges();
				if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
					m_curTab.switchRows(m_curTab.getCurrentRow(), m_curTab.getCurrentRow() - 1, m_curGC.getTable().getSortColumn(), m_curGC.getTable().isSortAscending());
				} else {
					m_curTab.navigateRelative(-1);
				}
			} 
			else if (cmd.equals(aSwitchLinesDownAction.getName())) 
			{
				//down-key + shift
				m_curGC.getTable().removeEditor();
				m_curTab.switchRows(m_curTab.getCurrentRow(), m_curTab.getCurrentRow() + 1, m_curGC.getTable().getSortColumn(), m_curGC.getTable().isSortAscending());
				m_curGC.getTable().requestFocus();
			} 
			else if (cmd.equals(aNext.getName())) 
			{ /* cmd_save(false); */
				//down-image + shift
				m_curGC.getTable().removeEditor();
				m_curGC.acceptEditorChanges();
				if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0) {
					m_curTab.switchRows(m_curTab.getCurrentRow(), m_curTab.getCurrentRow() + 1, m_curGC.getTable().getSortColumn(), m_curGC.getTable().isSortAscending());
				} else {
					m_curTab.navigateRelative(+1);
				}
			}
			else if (cmd.equals(aLast.getName()))
			{	/*cmd_save(false);*/
				m_curGC.getTable().removeEditor();
				m_curGC.acceptEditorChanges();
				m_curTab.navigate(m_curTab.getRowCount()-1);
			}
			else if (cmd.equals(aParent.getName()))
				cmd_parent();
			else if (cmd.equals(aDetail.getName()))
				cmd_detail();
			else if (cmd.equals(aZoomAcross.getName()))
				cmd_zoomAcross();
			else if (cmd.equals(aRequest.getName()))
				cmd_request();
			else if (cmd.equals(aArchive.getName()))
				cmd_archive();
			//	Tools
			else if (aWorkflow != null && cmd.equals(aWorkflow.getName()))
			{
				if (m_curTab.getRecord_ID() <= 0)
					;
				else if (m_curTab.getTabNo() == 0 && m_mWorkbench.getMWindow(getWindowIndex()).isTransaction())
					AEnv.startWorkflowProcess(m_curTab.getAD_Table_ID(), m_curTab.getRecord_ID());
				else
					AEnv.startWorkflowProcess(m_curTab.getAD_Table_ID(), m_curTab.getRecord_ID());
			}
			else if (aWinSize != null && cmd.equals(aWinSize.getName()))
				cmd_winSize();
			//	Help
			else if (cmd.equals(aHelp.getName()))
				cmd_help();
			//  General Commands (Environment)
			else if (cmd.equals(aLogout.getName()))
				cmd_logout();
			else if(WindowMenu.ShowAllWindows_ActionName.equals(cmd))
				m_WindowMenu.expose();
			else if (!AEnv.actionPerformed (e.getActionCommand(), m_curWindowNo, this))
				log.log(Level.SEVERE, "No action for: " + cmd);
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, cmd, ex);
			String msg = ex.getMessage();
			if (msg == null || msg.length() == 0)
				msg = ex.toString();
			msg = Services.get(IMsgBL.class).parseTranslation(m_ctx, msg);
			ADialog.error(m_curWindowNo, this, "Error", msg);
		}
		finally
		{
			if (requestFocus)
			{
				m_curWinTab.requestFocusInWindow();
			}
			setBusy(false, requestFocus);
		}
	}	//	actionPerformed

	private void cmd_logout()
	{
		JFrame top = Env.getWindow(Env.WINDOW_MAIN);
		if (top instanceof AMenu) {
			((AMenu)top).logout();
		}	
	}

	/**************************************************************************
	 *  Process Callout(s).
	 *  <p>
	 *  The Callout is in the string of
	 *  "class.method;class.method;"
	 * If there is no class name, i.e. only a method name, the class is regarded
	 * as CalloutSystem.
	 * The class needs to comply with the Interface Callout.
	 *
	 * @param field field
	 * @return error message or ""
	 * @see org.compiere.model.Callout
	 */
	private String processButtonCallout (VButton button)
	{
		// metas: 02553: begin: Make sure we are processing the callout from same tab
		//GridField field = m_curTab.getField(button.getColumnName());
		final GridField field = button.getField();
		if (field == null)
		{
			throw new AdempiereException("No GridField found for "+button);
		}
		final GridTab gridTab = field.getGridTab();
		if (gridTab != m_curTab)
		{
			log.warning("Processing a button callout from a different tab: curTab="+m_curTab+", button's tab="+gridTab);
		}
		// metas: 02553: end
		return m_curTab.processCallout(field);
	}	//	processButtonCallout
	
	/**
	 * Create (or copy to) a new record.
	 * 
	 * @param copyMode
	 */
	private void cmd_new(final DataNewCopyMode copyMode)
	{
		log.log(Level.CONFIG, "CopyMode={0}", copyMode);

		if (!m_curTab.isInsertRecord())
		{
			log.warning("Insert Record disabled for Tab");
			return;
		}
		
		m_curGC.stopEditor(true);
		m_curGC.acceptEditorChanges();
		
		// BF [ 2799362 ] attempt to save if save action is enabled. Using
		// m_curTab.needSave instead might miss unfilled mandatory fields.
		if (aSave.isEnabled())
		{
			//	Automatic Save
			if (Env.isAutoCommit(m_ctx, m_curWindowNo))
			{
				if (!cmd_save(true))
				{	
					return;
				}
			}
			//  explicitly ask when changing tabs
			else if (ADialog.ask(m_curWindowNo, this, "SaveChanges?", m_curTab.getCommitWarning()))
			{   //  yes we want to save
				if (!cmd_save(true))
				{   
					return;
				}
			}
			else    //  Don't save
				m_curTab.dataIgnore();
		}
		
		//
		// If we were asked to copy with details, ask the user which child tables he/she wants to be copied
		if (DataNewCopyMode.isCopyWithDetails(copyMode))
		{
			final List<TableInfoVO> childTablesToBeCopied = getSuggestedChildTablesToCopyWithDetails();
			// If user canceled then ignore everything and get out
			if (childTablesToBeCopied == null)
			{
				cmd_ignore();
				return;
			}

			m_curTab.setSuggestedCopyWithDetailsList(childTablesToBeCopied);
		}
		
		m_curTab.dataNew(copyMode);
		m_curGC.dynamicDisplay(0);
		// m_curTab.getTableModel().setChanged(false);
		
		//
		// If we are running in copy-with-details mode, we shall save the copied data now.
		// We expect the underlying PO which will be saved, to have the copy with details configured,
		// and the PO shall take control and copy the details (related records).
		if (DataNewCopyMode.isCopyWithDetails(copyMode))
		{
			cmd_save(true); // manualCmd=true
			// m_curTab.getTableModel().setCopyWithDetails(false);
			m_curTab.getTableModel().resetDataNewCopyMode();
		}
	}   //  cmd_new

	/**
	 *  Confirm & delete record
	 */
	private void cmd_delete()
	{
		if (m_curTab.isReadOnly())
			return;
		int keyID = m_curTab.getRecord_ID();
		if (ADialog.ask(m_curWindowNo, this, "DeleteRecord?"))
			if (m_curTab.dataDelete())
				m_curGC.rowChanged(false, keyID);
		m_curGC.dynamicDisplay(0);
	}   //  cmd_delete
	
	/**
	 * Show a list to select one or more items to delete.
	 */
	private void cmd_deleteSelection()
	{
		if (m_curTab.isReadOnly())
			return;
		
		//show table with deletion rows -> by identifiers columns FR [ 2877111 ]
		Vector<String> data = new Vector<String>();
		final String keyColumnName = m_curTab.getKeyColumnName();
		String sql = null;
		if (!Check.isEmpty(keyColumnName, true))
		{
			try
			{
				sql = MLookupFactory.getLookup_TableDirEmbed(Env.getLanguage(m_ctx), keyColumnName, "[?", "?]")
						.replace("[?.?]", "?");
			}
			catch (Exception e)
			{
				log.log(Level.WARNING, "Failed retrieving display info SQL for " + keyColumnName, e);
				sql = null;
			}
		}
		
		int noOfRows = m_curTab.getRowCount();
		for(int i = 0; i < noOfRows; i++)
		{
			final StringBuilder displayValue = new StringBuilder();
			if (Check.isEmpty(keyColumnName, true) || Check.isEmpty(sql, true))
			{
				final List<String> parentColumnNames = m_curTab.getParentColumnNames();
				for (final String columnName : parentColumnNames)
				{
					final GridField field = m_curTab.getField(columnName);
					if (field.isLookup())
					{
						Lookup lookup = field.getLookup();
						if (lookup != null)
						{
							displayValue.append(lookup.getDisplay(m_curTab.getValue(i, columnName))).append(" | ");
						}
						else
						{
							displayValue.append(m_curTab.getValue(i, columnName)).append(" | ");
						}
					}
					else
					{
						displayValue.append(m_curTab.getValue(i, columnName)).append(" | ");
					}
				}
			}
			else
			{
				final int id = m_curTab.getKeyID(i);
				String value = DB.getSQLValueStringEx(ITrx.TRXNAME_None, sql, id);
				if (value != null)
				value = value.replace(" - ", " | ");
				displayValue.append(value);
				// Append ID
				if (displayValue.length() == 0 || CLogMgt.isLevelFine())
				{
					if (displayValue.length() > 0)
						displayValue.append(" | ");
					displayValue.append("<").append(id).append(">");
				}
			}
			//
			data.add(displayValue.toString());
		}
		// FR [ 2877111 ]
		final JPanel messagePanel = new JPanel();
		final JList<String> list = new JList<>(data);
		final JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		messagePanel.add(scrollPane);
		
		final JOptionPane pane = new JOptionPane(
				messagePanel, // message
				JOptionPane.QUESTION_MESSAGE, // messageType
				JOptionPane.OK_CANCEL_OPTION); // optionType
		final JDialog deleteDialog = pane.createDialog(this.getParent(), Services.get(IMsgBL.class).getMsg(m_ctx, "DeleteSelection"));
		deleteDialog.setVisible(true);
		Integer okCancel = (Integer) pane.getValue();
		if(okCancel != null && okCancel == JOptionPane.OK_OPTION)
		{
			int[] indices = list.getSelectedIndices();
			Arrays.sort(indices);
			int offset = 0;
			for (int i = 0; i < indices.length; i++)
			{
				//m_curTab.setCurrentRow(indices[i]-offset);
				m_curTab.navigate(indices[i]-offset);
				int keyID = m_curTab.getRecord_ID();
				if (m_curTab.dataDelete())
				{
					m_curGC.rowChanged(false, keyID);
					offset++;
				}
			}
			m_curGC.dynamicDisplay(0);
		}
		else
		{
			log.fine("cancel");
		}
	}//cmd_deleteSelection

	/**
	 *  If required ask if you want to save and save it
	 *  @param manualCmd true if invoked manually (i.e. force)
	 *  @return true if saved
	 */
	public boolean cmd_save (boolean manualCmd)
	{
		if (m_curAPanelTab != null)
			manualCmd = false;
		log.config("Manual=" + manualCmd);
		m_errorDisplayed = false;
		m_curGC.stopEditor(true);
		m_curGC.acceptEditorChanges();

		if (m_curAPanelTab != null)
		{
			m_curAPanelTab.saveData();
			aSave.setEnabled(false);	//	set explicitly
		}

		if (m_curTab.getCommitWarning().length() > 0 && m_curTab.needSave(true, false))
			if (!ADialog.ask(m_curWindowNo, this, "SaveChanges?", m_curTab.getCommitWarning()))
				return false;

		//  manually initiated
		boolean retValue = m_curTab.dataSave(manualCmd);
		//   if there is no previous error
		if (manualCmd && !retValue && !m_errorDisplayed)
		{
			showLastError();
		}
		if (retValue)
		{
			m_curGC.rowChanged(true, m_curTab.getRecord_ID());
			// metas: cg: task 04771 start
			if (m_curTab.isOpen())
			{
				final int currentRow = m_curTab.getCurrentRow();
				if (currentRow != m_curGC.getTable().getSelectedRow())
				{
					m_curGC.getTable().setRowSelectionInterval(currentRow, currentRow);
				}
			}
			// metas: cg: task 04771 end
		}
		if (manualCmd) {
			m_curGC.dynamicDisplay(0);
			if (!isNested && m_window != null)
				m_window.setTitle(getTitle());
		}
		
		//BEGIN - [FR 1953734]
		if(m_curGC.isDetailGrid() && retValue){
			m_curGC.getGCParent().refreshMTab(m_curGC);
		}
		//END - [FR 1953734]
		
		return retValue;
	}   //  cmd_save

	private void showLastError() {
		String msg = CLogger.retrieveErrorString(null);
		if (msg != null)
			ADialog.error(m_curWindowNo, this, null, msg);
		else
			ADialog.error(m_curWindowNo, this, "SaveIgnored");
		setStatusLine(Services.get(IMsgBL.class).getMsg(m_ctx, "SaveIgnored"), true);
	}

	/**
	 *  Ignore
	 */
	private void cmd_ignore()
	{
		m_curGC.stopEditor(false);
		// Ignore changes in APanelTab (e.g. VSortTab) - teo_sarca [ 1705429 ] 
		if (m_curAPanelTab != null)
		{
			m_curAPanelTab.loadData();
		}
		m_curTab.dataIgnore();
		m_curTab.dataRefreshAll(); // metas
		m_curGC.dynamicDisplay(0);
		
	}   //  cmd_ignore

	/**
	 *  Refresh
	 */
	private void cmd_refresh()
	{
		cmd_save(false);
		m_curTab.dataRefreshAll();
		m_curGC.dynamicDisplay(0);
	}   //  cmd_refresh

	/**
	 *	Print standard Report
	 */
	private void cmd_report ()
	{
		log.info("");
		if (!Env.getUserRolePermissions().isCanReport(m_curTab.getAD_Table_ID()))
		{
			ADialog.error(m_curWindowNo, this, "AccessCannotReport");
			return;
		}
		
		cmd_save(false);

		//	Query
		final MQuery query = m_curTab.createReportingQuery(); // 03917

		new AReport (m_curTab.getAD_Table_ID(), aReport.getButton(), query, this, m_curWindowNo, getTabNo());
	}	//	cmd_report

	
	/**
	 * 	Zoom Across Menu
	 */
	private void cmd_zoomAcross()
	{
		int record_ID = m_curTab.getRecord_ID();
		log.info("ID=" + record_ID);
		if (record_ID <= 0)
			return;

		//	Query
		MQuery query = new MQuery();
		//	Current row
		String link = m_curTab.getKeyColumnName();
		//	Link for detail records
		if (link.length() == 0)
			link = m_curTab.getLinkColumnName();
		if (link.length() != 0)
		{
			if (link.endsWith("_ID"))
				query.addRestriction(link, MQuery.EQUAL, Env.getContextAsInt(m_ctx, m_curWindowNo, link));
			else
				query.addRestriction(link, MQuery.EQUAL, Env.getContext(m_ctx, m_curWindowNo, link));
		}
		new AZoomAcross(aZoomAcross.getButton(), m_curTab.getTableName(), m_curTab.getAD_Window_ID(), query);
	}	//	cmd_zoom
	
	/**
	 * 	Open/View Request
	 */
	private void cmd_request()
	{
		int record_ID = m_curTab.getRecord_ID();
		log.info("ID=" + record_ID);
		if (record_ID <= 0)
			return;

		int AD_Table_ID = m_curTab.getAD_Table_ID();
		int C_BPartner_ID = 0;
		Object BPartner_ID = m_curTab.getValue("C_BPartner_ID");
		if (BPartner_ID != null)
			C_BPartner_ID = ((Integer)BPartner_ID).intValue();
		new ARequest (aRequest.getButton(), AD_Table_ID, record_ID, C_BPartner_ID);
	}	//	cmd_request

	/**
	 * 	Open/View Archive
	 */
	private void cmd_archive()
	{
		int record_ID = m_curTab.getRecord_ID();
		log.info("ID=" + record_ID);
		if (record_ID <= 0)
			return;

		int AD_Table_ID = m_curTab.getAD_Table_ID();
		new AArchive (aArchive.getButton(), AD_Table_ID, record_ID);
	}	//	cmd_archive
	
	/**
	 *	Print specific Report - or start default Report
	 */
	private void cmd_print()
	{
		cmd_print(false);
	}
	
	/**
	 *	Print specific Report - or start default Report
	 */
	private void cmd_print(boolean printPreview)
	{
		//	Get process defined for this tab
		int AD_Process_ID = m_curTab.getAD_Process_ID();
		log.info("ID=" + AD_Process_ID);

		//	No report defined
		if (AD_Process_ID == 0)
		{
			cmd_report();
			return;
		}

		cmd_save(false);
		//
		int table_ID = m_curTab.getAD_Table_ID();
		int record_ID = m_curTab.getRecord_ID();
		ProcessInfo pi = new ProcessInfo (getTitle(), AD_Process_ID, table_ID, record_ID);
		pi.setAD_User_ID (Env.getAD_User_ID(m_ctx));
		pi.setAD_Client_ID (Env.getAD_Client_ID(m_ctx));
		pi.setPrintPreview(printPreview);
		pi.setWindowNo(m_curWindowNo); // metas: 03040
		pi.setTabNo(getTabNo());

		ProcessCtl.process(this, m_curWindowNo, pi, ITrx.TRX_None); //  calls lockUI, unlockUI
		statusBar.setStatusLine(pi.getSummary(), pi.isError());
	}   //  cmd_print

	/**
	 *	Find - Set Query
	 */
	private void cmd_find()
	{
		//
		// Use the embedded find panel if available (metas-2009_0021_AP1_G113)
		final FindPanelContainer findPanel = m_curGC != null ? m_curGC.getFindPanel() : null;
		if (findPanel != null)
		{
			findPanel.setExpanded(!findPanel.isExpanded()); // toggle expanded flag
			aFind.setPressed(findPanel.isExpanded());
		}
		//
		// Use the Find dialog
		else
		{
			if (m_curTab == null)
				return;
			cmd_save(false);
			
			Find find = Find.builder()
					.setGridController(m_curGC)
					.setMinRecords(1)
					.buildFindDialog();
			MQuery query = find.getQuery();
			find.dispose();
			find = null;
	
			//	Confirmed query
			if (query != null)
			{
				m_onlyCurrentRows = false;      	//  search history too
				m_curTab.setQuery(query);
				m_curGC.query(m_onlyCurrentRows, m_onlyCurrentDays, GridTabMaxRows.NO_RESTRICTION);   //  autoSize
			}
			aFind.setPressed(m_curTab.isQueryActive());
		}
	}	//	cmd_find

	/**
	 *	Attachment
	 */
	private void cmd_attachment()
	{
		int record_ID = m_curTab.getRecord_ID();
		log.info("Record_ID=" + record_ID);
		if (record_ID == -1)	//	No Key
		{
			aAttachment.setEnabled(false);
			return;
		}

	//	Attachment va = 
		new Attachment (getCurrentFrame(), m_curWindowNo,
			m_curTab.getAD_AttachmentID(), m_curTab.getAD_Table_ID(), record_ID, null);
		//
		m_curTab.loadAttachments();				//	reload
		aAttachment.setPressed(m_curTab.hasAttachment());
	}	//	attachment

	/**
	 *	Chat
	 */
	private void cmd_chat()
	{
		int record_ID = m_curTab.getRecord_ID();
		log.info("Record_ID=" + record_ID);
		if (record_ID == -1)	//	No Key
		{
			aChat.setEnabled(false);
			return;
		}
		//	Find display
		String infoName = null;
		String infoDisplay = null;
		for (int i = 0; i < m_curTab.getFieldCount(); i++)
		{
			GridField field = m_curTab.getField(i);
			if (field.isKey())
				infoName = field.getHeader();
			if ((field.getColumnName().equals("Name") || field.getColumnName().equals("DocumentNo") )
				&& field.getValue() != null)
				infoDisplay = field.getValue().toString();
			if (infoName != null && infoDisplay != null)
				break;
		}
		String description = infoName + ": " + infoDisplay;
		//
	//	AChat va = 
		new AChat (getCurrentFrame(), m_curWindowNo,
			m_curTab.getCM_ChatID(), m_curTab.getAD_Table_ID(), record_ID, 
			description, null);
		//
		m_curTab.loadChats();				//	reload
		aChat.setPressed(m_curTab.hasChat());
	}	//	chat

	/**
	 *	Lock
	 */
	private void cmd_lock()
	{
		log.info("Modifiers=" + m_lastModifiers);
		if (!m_isPersonalLock)
			return;
		int record_ID = m_curTab.getRecord_ID();
		if (record_ID == -1)	//	No Key
			return;
		//	Control Pressed
		if ((m_lastModifiers & InputEvent.CTRL_MASK) != 0)
		{
			new RecordAccessDialog(getCurrentFrame(), m_curTab.getAD_Table_ID(), record_ID);
		}
		else
		{
			m_curTab.lock (Env.getCtx(), record_ID, aLock.getButton().isSelected());
			m_curTab.loadAttachments();			//	reload
		}
		aLock.setPressed(m_curTab.isLocked());
	}	//	lock

	/**
	 *	Toggle History
	 */
	private void cmd_history()
	{
		log.info("");
		if (m_mWorkbench.getMWindow(getWindowIndex()).isTransaction())
		{
			if (m_curTab.needSave(true, true) && !cmd_save(false))
				return;

			Point pt = new Point (0, aHistory.getButton().getBounds().height);
			SwingUtilities.convertPointToScreen(pt, aHistory.getButton());
			VOnlyCurrentDays ocd = new VOnlyCurrentDays(getCurrentFrame(), pt);
			if (!ocd.isCancel()) {
				m_onlyCurrentDays = ocd.getCurrentDays();
				if (m_onlyCurrentDays == 1)	//	Day
				{
					m_onlyCurrentRows = true;
					m_onlyCurrentDays = 0; 	//	no Created restriction
				}
				else
					m_onlyCurrentRows = false;
				//
				m_curTab.setQuery(null);	//	reset previous queries
				//
				log.config("OnlyCurrent=" + m_onlyCurrentRows + ", Days=" + m_onlyCurrentDays);
				m_curGC.query(m_onlyCurrentRows, m_onlyCurrentDays, GridTabMaxRows.DEFAULT);   //  autoSize
			}
			// Restore history button's pressed status
			else {
				if (isFirstTab())
					aHistory.setPressed(!m_curTab.isOnlyCurrentRows());
			}
		}
	}	//	cmd_history

	/**
	 *	Help
	 */
	private void cmd_help()
	{
		log.info("");
		Help hlp = new Help (getCurrentFrame(), this.getTitle(), m_mWorkbench.getMWindow(getWindowIndex()));
		hlp.setVisible(true);
	}	//	cmd_help

	/**
	 * Close this screen - after save
	 * 
	 * @param doExitApplication true if the entire application shall be closed
	 */
	private void cmd_end(final boolean doExitApplication)
	{
		if (aSave.isEnabled() // 07315: we are trying to save only if save button is active (why? ...see the test case from task page)
				&& !cmd_save(false))
		{
			return;
		}

		// If we were asked to exit the application then ask the user to confirm that
		if (doExitApplication && !ADialog.ask(m_curWindowNo, this, "ExitApplication?"))
		{
			return; // user canceled, do nothing
		}

		getCurrentFrame().dispose();		// calls this dispose
		
		if (doExitApplication)
		{
			AEnv.exit(0);
		}
	}   // cmd_end

	/**
	 * 	Set Window Size
	 */
	private void cmd_winSize()
	{
		Dimension size = getSize();
		if (!ADialog.ask(m_curWindowNo, this, "WinSizeSet", 
			"x=" + size.width + " - y=" + size.height))
		{
			setPreferredSize(null);
			SwingUtilities.getWindowAncestor(this).pack();
			size = new Dimension (0,0);
		}
		//
		MWindow win = new MWindow(m_ctx, m_curTab.getAD_Window_ID(), null);
		win.setWindowSize(size);
		win.save();
	}	//	cmdWinSize
	
	private void cmd_export()
	{
		new AExport(this);
	}
	
	private void cmd_home()
	{
		// Actually show the window in another event to make sure it's the last one (see 07315)
		EventQueue.invokeLater(new Runnable()
		{
			
			@Override
			public void run()
			{
				AEnv.showWindow(Env.getWindow(Env.WINDOW_MAIN));
			}
		});
	}
	
	
	/**************************************************************************
	 *	Start Button Process
	 *  @param vButton button
	 */
	// metas: tsa: changed to protected
		protected void actionButton (final VButton vButton)
	{
		try
		{
			actionButton0(vButton);
		}
		catch (Exception e)
		{
			Services.get(IClientUI.class).error(m_curWindowNo, e);
		}
	}

	private final void actionButton0 (final VButton vButton) throws Exception
	{
		log.log(Level.INFO, "{0}", vButton);

		if (m_curTab.hasChangedCurrentTabAndParents())
		{
			final String msg = CLogger.retrieveErrorString("Please ReQuery Window");
			throw new AdempiereException(msg);
		}

		boolean startWOasking = false;
		final String columnName = vButton.getColumnName();

		//  Zoom
		if (columnName.equals("Record_ID"))
		{
			int AD_Table_ID = Env.getContextAsInt (m_ctx, m_curWindowNo, "AD_Table_ID");
			int Record_ID = Env.getContextAsInt (m_ctx, m_curWindowNo, "Record_ID");
			AEnv.zoom(AD_Table_ID, Record_ID);
			return;
		}   //  Zoom

		//  save first	---------------
		if (m_curTab.needSave(true, false))
			if (!cmd_save(true))
				return;
		// Save included tabs if necessary - teo_sarca BF [ 2876892 ]
		if (Env.isAutoCommit(m_ctx, m_curWindowNo)) // metas
		{
			for (GridTab includedTab : m_curTab.getIncludedTabs())
			{
				if (includedTab.needSave(true, false))
					if(!includedTab.dataSave(false)) // metas: changed from true to false, see us1165
						return;
			}
		}
		// metas: tsa: us1010: we need to update the context because if there are some included tabs
		// it's a big change to have their values in context, and so the process default parameters (if they refer to context)
		// will not be correct
		m_curTab.updateContext(); // metas
		//
		final int table_ID = m_curTab.getAD_Table_ID();
		//	Record_ID
		int record_ID = m_curTab.getRecord_ID();
		//	Record_ID - Language Handling
		if (record_ID == -1 && m_curTab.getKeyColumnName().equals("AD_Language"))
			record_ID = Env.getContextAsInt (m_ctx, m_curWindowNo, "AD_Language_ID");
		//	Record_ID - Change Log ID
		if (record_ID == -1 
			&& (vButton.getProcess_ID() == 306 || vButton.getProcess_ID() == 307))
		{
			Integer id = (Integer)m_curTab.getValue("AD_ChangeLog_ID");
			record_ID = id.intValue();
		}
		final boolean noRowFound = record_ID == -1 && m_curTab.getKeyColumnName().endsWith("_ID");
		
		boolean isProcessMandatory = false;

		//	Pop up Payment Rules
		if (columnName.equals("PaymentRule"))
		{
			//	Ensure it's saved
			if(noRowFound)
			{
				throw new AdempiereException("@SaveErrorRowNotFound@");
			}
			
			final VPayment vp = new VPayment(m_curWindowNo, m_curTab, vButton);
			if (vp.isInitOK())		//	may not be allowed
				vp.setVisible(true);
			vp.dispose();
			if (vp.needSave())
			{
				cmd_save(false);
				cmd_refresh();
			}
		}	//	PaymentRule

		//	Pop up Document Action (Workflow)
		else if (columnName.equals("DocAction"))
		{
			//	Ensure it's saved
			if(noRowFound)
		{
				throw new AdempiereException("@SaveErrorRowNotFound@");
			}

			isProcessMandatory = true;
			final VDocAction vda = new VDocAction(m_curWindowNo, m_curTab, vButton, record_ID);
			//	Something to select from?
			if (vda.getNumberOfOptions() == 0)
			{
				vda.dispose ();
				log.info("DocAction - No Options");
				return;
			}
			else
			{
				vda.setVisible(true);
				if (!vda.isStartProcess())
					return;
//				batch = vda.isBatch();
				startWOasking = true;
				vda.dispose();
			}
		}	//	DocAction

		//  Pop up Create From
		else if (columnName.equals("CreateFrom"))
		{
			//	Ensure it's saved
			if(noRowFound)
		{
				throw new AdempiereException("@SaveErrorRowNotFound@");
			}
			
			// Run form only if the button has no process defined - teo_sarca [ 1974354 ] 
			if (vButton.getProcess_ID() <= 0)
			{
				ICreateFrom cf = VCreateFromFactory.create(m_curTab);
				if(cf != null)
				{
					if(cf.isInitOK())
					{
						cf.showWindow();
						cf.closeWindow();
						m_curTab.dataRefresh();
					}
					else
						cf.closeWindow();
					return;
				}				
				//	else may start process
			}
		}	//	CreateFrom

		//  Posting -----
		else if (columnName.equals("Posted") && Env.getUserRolePermissions().hasPermission(IUserRolePermissions.PERMISSION_ShowAcct))
		{
			//	Ensure it's saved
			if(noRowFound)
		{
				throw new AdempiereException("@SaveErrorRowNotFound@");
			}
			
			//  Check Doc Status
			final String processed = Env.getContext(m_ctx, m_curWindowNo, "Processed");
			if (!"Y".equals(processed))
			{
				String docStatus = Env.getContext(m_ctx, m_curWindowNo, "DocStatus");
				if (DocAction.STATUS_Completed.equals(docStatus)
					|| DocAction.STATUS_Closed.equals(docStatus)
					|| DocAction.STATUS_Reversed.equals(docStatus)
					|| DocAction.STATUS_Voided.equals(docStatus))
					;
				else
				{
					throw new AdempiereException("@PostDocNotComplete@");
				}
			}

			// try to get table and record id from context data (eg for unposted view)
			// otherwise use current table/record
			int tableId = Env.getContextAsInt(m_ctx, m_curWindowNo, "AD_Table_ID", true);
			int recordId = Env.getContextAsInt(m_ctx, m_curWindowNo, "Record_ID", true);
			if ( tableId == 0 || recordId == 0 )
			{
				tableId = m_curTab.getAD_Table_ID();
				recordId = m_curTab.getRecord_ID();
			}

			//  Check Post Status
			Object ps = m_curTab.getValue("Posted");
			if (ps != null && ps.equals("Y"))
			{
				new org.compiere.acct.AcctViewer (Env.getContextAsInt (m_ctx, m_curWindowNo, "AD_Client_ID"), tableId, recordId);
			}
			else
			{
				if (ADialog.ask(m_curWindowNo, this, "PostImmediate?"))
				{
					boolean force = ps != null && !ps.equals ("N");		//	force when problems
					AEnv.postImmediate (m_curWindowNo, Env.getAD_Client_ID(m_ctx), tableId, recordId, force);
					cmd_refresh();
				}
			}
			return;
		}   //  Posted

		/**
		 *  Start Process ----
		 *  or invoke user form
		 */

		log.config("Process_ID=" + vButton.getProcess_ID() + ", Record_ID=" + record_ID);
		if (vButton.getProcess_ID() <= 0)
		{
			if (isProcessMandatory)
			{
				throw new AdempiereException("@NotFound@ @AD_Process_ID@");
			}
			return;
		}
		
		//	Save item changed
		if (m_curTab.needSave(true, false))
			if (!cmd_save(true))
				return;
		
		//
		// Load and check process requirements
		final I_AD_Process process = InterfaceWrapperHelper.create(Env.getCtx(), vButton.getProcess_ID(), I_AD_Process.class, ITrx.TRXNAME_None);
		final ProcessClassInfo processClassInfo = ProcessClassInfo.ofClassname(process.getClassname());
		if (processClassInfo.isExistingCurrentRecordRequiredWhenCalledFromGear() && noRowFound)
		{
			throw new AdempiereException("@SaveErrorRowNotFound@");
		}
		
		//
		// Call Form
		int form_ID = process.getAD_Form_ID();
		if (form_ID > 0) 
		{
			if (m_curTab.needSave(true, false))
				if (!cmd_save(true))
					return;
			
			FormFrame ff = new FormFrame(); 
			String title = vButton.getDescription();
			if (title == null || title.length() == 0)
				title = vButton.getName();
			final ProcessInfo pi = new ProcessInfo (title, vButton.getProcess_ID(), table_ID, record_ID);
			pi.setWindowNo(m_curWindowNo);
			pi.setTabNo(getTabNo());
			pi.setAD_User_ID (Env.getAD_User_ID(m_ctx));
			pi.setAD_Client_ID (Env.getAD_Client_ID(m_ctx));
			pi.setWhereClause(m_curTab.getTableModel().getSelectWhereClauseFinal());
			pi.setGridTabSummaryInfo(m_curTab.getSummaryInfo());
			ff.setProcessInfo(pi);
			ff.addWindowListener(new WindowAdapter()
			{
				/** When form frame is closed we want to refresh(all) records is needed */
				@Override
				public void windowClosed(WindowEvent e)
				{
					unlockUI(pi);
				}
			});
			if (ff.openForm(form_ID))
			{
				ff.pack();

				final I_AD_Form form = process.getAD_Form();
				if (!form.isModal())
				{
					AEnv.showCenterScreen(ff);
				}
				else
				{
					final JFrame parentFrame = getCurrentFrame();
					final Runnable onCloseCallback = null; // no callback (TODO? maybe have a class to be loaded)
					AEnv.showCenterWindowModal(ff, parentFrame, onCloseCallback);
				}
			}
			else
			{
				ff.dispose();
				ff = null;
			}
			return; 
		}
		//
		// Call Process
		else
		{
			final ProcessModalDialog dialog = new ProcessModalDialog(
					m_curTab, // gridTab
					this, // ASyncProcess aProcess
					vButton.getProcess_ID(), // AD_Process_ID
					table_ID, record_ID, // AD_Table_ID, Record_ID
					startWOasking  // autoStart
			);
			if (dialog.isValidDialog())
			{
				dialog.validate();
				dialog.pack();
				AEnv.showCenterWindow(Env.getWindow(m_curWindowNo), dialog);
			}
		}
	}	//	actionButton

	private JFrame getCurrentFrame()
	{
		return Env.getFrame(this);
	}

	/**************************************************************************
	 *  Lock User Interface.
	 *  Called from the Worker before processing
	 *  @param pi process info
	 */
	@Override
	public void lockUI (ProcessInfo pi)
	{
	//	log.fine("" + pi);
		setBusy(true, false);
	}   //  lockUI

	/**
	 * Unlock User Interface. Called from the Worker when processing is done
	 * 
	 * @param pi of execute ASync call
	 */
	@Override
	public void unlockUI(final ProcessInfo pi)
	{
		// Do nothing if disposing or already disposed.
		if (m_disposing)
		{
			return;
		}

		// log.fine("" + pi);
		final boolean notPrint = pi != null
				&& pi.getAD_Process_ID() != m_curTab.getAD_Process_ID()
				&& pi.isReportingProcess() == false;

		//
		setBusy(false, notPrint);

		// Process Result
		if (notPrint) // refresh if not print
		{
			// Refresh data
			if (pi.isRefreshAllAfterExecution())
			{
				m_curTab.dataRefreshAll();
			}
			else
			{
				m_curTab.dataRefresh();
			}

			// Timeout
			if (pi.isTimeout())		// set temporarily to R/O
			{
				Env.setContext(m_ctx, m_curWindowNo, "Processed", "Y");
			}

			m_curGC.dynamicDisplay(0);

			updateStatusLine(pi);

			// Show process logs if any
			if (pi.isShowProcessLogs())
			{
				ProcessInfoUtil.setLogFromDB(pi);
	
				final String logInfo = pi.getLogInfo();
				if (logInfo.length() > 0)
				{
					ADialog.info(m_curWindowNo, this, Env.getHeader(m_ctx, m_curWindowNo), pi.getTitle(), logInfo);	// clear text
				}
			}
		}
		else
		{
			updateStatusLine(pi);
		}
	}   // unlockUI
	
	private final void updateStatusLine(final ProcessInfo pi)
	{
		// Update Status Line
		setStatusLine(pi.getSummary(), pi.isError());
		if (pi.isError())
		{
			ADialog.error(m_curWindowNo, this, null, pi.getSummary());
		}
	}

	/**
	 *  Is the UI locked (Internal method)
	 *  @return true, if UI is locked
	 */
	@Override
	public boolean isUILocked()
	{
		return m_isLocked;
	}   //  isLoacked

	/**
	 *  Method to be executed async.
	 *  Called from the ASyncProcess worker
	 *  @param pi process info
	 */
	@Override
	public void executeASync (ProcessInfo pi)
	{
		log.config("-");
	}   //  executeASync

	/**
	 * 	Get Current Tab
	 *	@return current tab
	 */
	public GridTab getCurrentTab()
	{
		return m_curTab;
	}	//	getCurrentTab
	
	/**
	 * Get the number of tabs in the panels JTabbedPane.
	 * @return no of tabs in the JTabbedPane of the panel
	 */
	public int noOfTabs() {
 		return m_curWinTab.getTabCount();
	}
	
	/**
	 * Get the selected tab index of the panels JTabbedPane.
	 * @return selected index of JTabbedPane
	 */
	public int getSelectedTabIndex() {
 		return m_curWinTab.getSelectedIndex();
	}
	
	/**
	 * Set the tab index of the panels JTabbedPane.
	 */
	public void setSelectedTabIndex(int index) {
 		m_curWinTab.setSelectedIndex(index);
	}
	
	/**
	 * Get the name of the selected tab in the panels JTabbedPane.
	 * @return name of selected tab
	 */
	public String getSelectedTabName()
	{
 		String title = m_curWinTab.getTitleAt(m_curWinTab.getSelectedIndex());
 		title = title.substring(title.indexOf("<html>")+6);
 		title = title.substring(0,title.indexOf('<'));
 		return title;
	}
	
	/**
	 *  String representation
	 *  @return String representation
	 */
	@Override
	public String toString()
	{
		String s = "APanel[curWindowNo=" + m_curWindowNo;
		if (m_mWorkbench != null)
			s += ",WB=" + m_mWorkbench.toString();
		// metas: begin
		if (m_curTab != null)
			s += ", GridTab=" + m_curTab;
		// metas: end
		s += "]";
		return s;
	}   //  toString

	/**
	 * Simple action class for the resort of tablelines (switch line no). Delegates actionPerformed
	 * to APanel.
	 * 
	 * @author Karsten Thiemann, kthiemann@adempiere.org
	 * 
	 */
	class SwitchAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3837712049468116744L;

		/** the action listener - APanel */
		private ActionListener al;

		/** action name */
		private String name;

		/**
		 * Constructor.
		 * @param name
		 * @param accelerator
		 * @param al
		 */
		SwitchAction(String name, KeyStroke accelerator, ActionListener al) {
			super(name);
			putValue(Action.NAME, name); // Display
			putValue(Action.SHORT_DESCRIPTION, name); // Tooltip
			putValue(Action.ACCELERATOR_KEY, accelerator); // KeyStroke
			putValue(Action.ACTION_COMMAND_KEY, name); // ActionCammand
			this.al = al;
			this.name = name;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			al.actionPerformed(e);
		} // actionPerformed

		public String getName() {
			return name;
		}
	}

	/**
	 * Removes the default KeyStroke action for the up/down keys and adds switch
	 * line actions.
	 */
	private void initSwitchLineAction()
	{
		final KeyStroke keyLinesDown = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.SHIFT_MASK);
		final KeyStroke keyLinesUp = KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.SHIFT_MASK);
		
		aSwitchLinesDownAction = new SwitchAction("switchLinesDown", keyLinesDown, this);
		aSwitchLinesUpAction = new SwitchAction("switchLinesUp", keyLinesUp, this);

		final JTable table = m_curGC.getTable();
		table.getInputMap(CPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyLinesDown, "none");
		table.getInputMap(CPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyLinesUp, "none");
		table.getInputMap(CPanel.WHEN_FOCUSED).put(keyLinesDown, "none");
		table.getInputMap(CPanel.WHEN_FOCUSED).put(keyLinesUp, "none");

		getInputMap(CPanel.WHEN_IN_FOCUSED_WINDOW).put(keyLinesDown, aSwitchLinesDownAction.getName());
		getActionMap().put(aSwitchLinesDownAction.getName(), aSwitchLinesDownAction);
		getInputMap(CPanel.WHEN_IN_FOCUSED_WINDOW).put(keyLinesUp, aSwitchLinesUpAction.getName());
		getActionMap().put(aSwitchLinesUpAction.getName(), aSwitchLinesUpAction);
	}
	
// metas: begin
	public static final String CMD_Ignore = "Ignore"; // metas  02029
	
	private boolean isSearchActive = true; // metas-2009_0021_AP1_CR057
	private boolean isTabIncluded = false; // metas-2009_0021_AP1_CR056
	private AppsAction aCopyDetails; // metas
	
	public GridWorkbench getGridWorkbench()
	{
		return m_mWorkbench;
	}

	@Override
	public void processEvent(final ProcessEvent event)
	{

		if (!ProcessEvent.EventType.trxFinished.equals(event.getType())) {
			return;
		}
		if (!(event.getSource() instanceof PO)) {
			return;
		}
		final PO sourcePO = (PO) event.getSource();

		int currentTabAdTableId = -1;
		try {
			currentTabAdTableId = m_curTab.getAD_Table_ID();
		} catch (final NullPointerException e) {
			// TODO: this the NPE is only thrown if m_curTab.dispose() that been
			// called. But then, why hasn't this panel's dispose() method been
			// called as well?
			return;
		}
		if (m_curTab == null || sourcePO.get_Table_ID() != currentTabAdTableId) {
			return;
		}
		cmd_refresh();
	}

	// metas-2009_0021_AP1_CR064
	@Override
	public void requestFocus()
	{
		m_curGC.requestFocus();
	}

	@Override
	public boolean requestFocusInWindow()
	{
		return m_curGC.requestFocusInWindow();
	}
	
	/**
	 * @return list of {@link TableInfoVO} to be also copied or <code>null</code> if user canceled.
	 */
	private final List<TableInfoVO> getSuggestedChildTablesToCopyWithDetails()
	{
		final JPanel messagePanel = new JPanel();
		final JList<String> list = new JList<String>();
		final JScrollPane scrollPane = new JScrollPane(list);
		final Vector<String> data = new Vector<String>();
		final CopyRecordSupport childCRS = CopyRecordFactory.getCopyRecordSupport(m_curTab.getTableName());
		PO po = TableModelLoader.instance.getPO(m_ctx, m_curTab.getTableName(), m_curTab.getRecord_ID(), ITrx.TRXNAME_None);
		final List<TableInfoVO> tiList = new ArrayList<TableInfoVO>();
		for (final TableInfoVO ti : childCRS.getSuggestedChildren(po, m_curTab))
		{
			tiList.add(ti);
			final StringBuilder displayValue = new StringBuilder();
			displayValue.append(ti.getName());
			//
			data.add(displayValue.toString());
		}
		//
		list.setListData(data);
		//
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// select entire list
		int[] rv = new int[data.size()];
		for (int i = 0; i < data.size(); i++)
			rv[i] = i;
		list.setSelectedIndices(rv);
		//
		messagePanel.add(scrollPane);

		final JOptionPane pane = new JOptionPane(
				messagePanel, // message
				JOptionPane.QUESTION_MESSAGE, // messageType
				JOptionPane.OK_CANCEL_OPTION); // optionType
		final JDialog deleteDialog = pane.createDialog(this.getParent(), Services.get(IMsgBL.class).getMsg(m_ctx, "CopyDetailsSelection"));
		deleteDialog.setVisible(true);
		final Integer okCancel = (Integer)pane.getValue();
		if (okCancel != null && okCancel == JOptionPane.OK_OPTION)
		{
			final int[] indices = list.getSelectedIndices();
			Arrays.sort(indices);
			final ImmutableList.Builder<TableInfoVO> suggestedList = ImmutableList.builder();
			for (int i = 0; i < indices.length; i++)
			{
				suggestedList.add(tiList.get(indices[i]));
			}

			return suggestedList.build();
		}
		else
		{
			return null; // canceled
		}
	}
	
	public GridController getCurrentGridController()
	{
		return this.m_curGC;
	}
	
	public final AppsAction getIgnoreAction()
	{
		return aIgnore;
	}
	
	public final boolean isAlignVerticalTabsWithHorizontalTabs()
	{
		return alignVerticalTabsWithHorizontalTabs;
	}
	
	/**
	 * For a given component, it removes component's key bindings (defined in ancestor's map) that this panel also have defined.
	 * 
	 * The main purpose of doing this is to make sure menu/toolbar key bindings will not be intercepted by given component, so panel's key bindings will work when given component is embedded in our
	 * panel.
	 * 
	 * @param comp component or <code>null</code>
	 */
	public final void removeAncestorKeyBindingsOf(final JComponent comp, final Predicate<KeyStroke> isRemoveKeyStrokePredicate)
	{
		// Skip null components (but tolerate that)
		if(comp == null)
		{
			return;
		}

		// Get component's ancestors input map (the map which defines which key bindings will work when pressed in a component which is included inside this component).
		final InputMap compInputMap = comp.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		// NOTE: don't check and exit if "compInputMap.size() <= 0" because it might be that this map is empty but the key bindings are defined in it's parent map. 

		// Iterate all key bindings for our panel and remove those which are also present in component's ancestor map.
		final InputMap thisInputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		for (final KeyStroke key : thisInputMap.allKeys())
		{
			// Check if the component has a key binding defined for our panel key binding.
			final Object compAction = compInputMap.get(key);
			if (compAction == null)
			{
				continue;
			}
			
			if (isRemoveKeyStrokePredicate != null && !isRemoveKeyStrokePredicate.apply(key))
			{
				continue;
			}

			// NOTE: Instead of removing it, it is much more safe to bind it to "none",
			// to explicitly say to not consume that key event even if is defined in some parent map of this input map.
			compInputMap.put(key, "none");

			if (log.isLoggable(Level.FINE))
			{
				log.fine("Removed " + key + "->" + compAction + " which in this component is binded to " + thisInputMap.get(key)
						+ "\n\tThis panel: " + this
						+ "\n\tComponent: " + comp);
			}
		}
	}
}	//	APanel
