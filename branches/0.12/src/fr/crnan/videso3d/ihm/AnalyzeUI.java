/*
 * This file is part of ViDESO.
 * ViDESO is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ViDESO is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ViDESO.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.crnan.videso3d.ihm;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.DockController;
import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.action.DockActionIcon;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.MultipleCDockableLayout;
import bibliothek.gui.dock.common.action.CAction;
import bibliothek.gui.dock.common.action.core.CommonDropDownItem;
import bibliothek.gui.dock.common.action.predefined.CCloseAction;
import bibliothek.gui.dock.common.event.CFocusListener;
import bibliothek.gui.dock.common.group.CGroupBehavior;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.CommonDockable;
import bibliothek.gui.dock.common.intern.action.CDropDownItem;
import bibliothek.gui.dock.common.theme.CEclipseTheme;
import bibliothek.gui.dock.common.theme.ThemeMap;
import bibliothek.gui.dock.util.Priority;
import bibliothek.util.xml.XElement;

import fr.crnan.videso3d.DatabaseManager;
import fr.crnan.videso3d.Videso3D;
import fr.crnan.videso3d.graphs.ConnexPanel;
import fr.crnan.videso3d.graphs.ItiPanel;
import fr.crnan.videso3d.graphs.ResultGraphPanel;
import fr.crnan.videso3d.graphs.RoutePanel;
import fr.crnan.videso3d.graphs.StarPanel;
import fr.crnan.videso3d.graphs.TrajetPanel;
import fr.crnan.videso3d.stip.StipController;
import glass.eclipse.theme.CGlassEclipseTabPainter;
import glass.eclipse.theme.CGlassStationPaint;
import glass.eclipse.theme.CMiniPreviewMovingImageFactory;
import glass.eclipse.theme.EclipseThemeExtension;
/**
 * Fenêtre d'analyse des données Stip et Stpv.<br />
 * Cette classe est un singleton afin de n'être ouverte qu'une fois maximum.
 * @author Bruno Spyckerelle
 * @version 0.4.0
 */
public final class AnalyzeUI extends JFrame {

	private static AnalyzeUI instance = null;

	private ContextPanel context = new ContextPanel();
	
	private CControl control;
	private ResultFactory factory = new ResultFactory();
	
	private JSplitPane splitpane;

	private JLabel nombreResultats = new JLabel();
	
	private JPanel searchPanelContainer = new JPanel(new CardLayout(0, 0));
	private JPanel topPanel;
	
	private AdvancedSearchPanel itiSearch, baliseSearch = null;

	private JButton advancedSearch;

	private SearchPanel searchPanel;
	
	public final static AnalyzeUI getInstance(){
		if(instance == null){
			instance = new AnalyzeUI();
		}
		return instance;
	}

	public static void showAnalyzeUI(){
		getInstance().setVisible(true);
	}


	/**
	 * Ajoute un tab de résultats et ouvre la fenêtre si besoin
	 * @param type Type de recherche
	 * @param balise1 Première balise cherchée
	 * @param balise2 Deuxième balise cherchée (optionnel)
	 * @param numero Numéro de la liaison privilégiée recherchée (optionnel)
	 */
	public final static void showResults(boolean advanced, String type, String... criteria){		
		final ResultPanel content = getInstance().createResultPanel(advanced, type, /*getInstance().tabPane,*/ criteria);
		content.setContext(getInstance().context);
		
		final ResultDockable dockable = getInstance().new ResultDockable(getInstance().factory, content);
		dockable.setCloseable(true);
		dockable.setRemoveOnClose(true);
		dockable.setLocation(CLocation.base().normal());
		dockable.setTitleText(content.getTitleTab());
		
		dockable.addAction(getInstance().new AllCloseAction(getInstance().control));
		
		getInstance().control.addDockable(dockable);
		dockable.setVisible(true);
		
		//mise à jour du titre de l'onglet avec le nombre de résultats
		content.addPropertyChangeListener(ResultPanel.PROPERTY_RESULT, new PropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				dockable.setTitleText(content.getTitleTab()+" ("+evt.getNewValue()+")");
				getInstance().nombreResultats.setText(evt.getNewValue().toString());
			}
			
		});
		
		//certains panel gèrent eux-mêmes leur titre
		content.addPropertyChangeListener(ResultPanel.TITLE_TAB_NAME, new PropertyChangeListener() {
			
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				dockable.setTitleText((String)evt.getNewValue());
			}
		});
		
		//remettre les infos contextuelles de la balise lorsque l'on clique sur un onglet balise
		dockable.addFocusListener(new CFocusListener() {
			
			@Override
			public void focusLost(CDockable dockable) {}
			
			@Override
			public void focusGained(CDockable dock) {
				if(dock instanceof ResultDockable){
					if(((ResultDockable)dock).getTitleText().startsWith("Balise")){
						getContextPanel().showInfo(DatabaseManager.Type.STIP, StipController.BALISES, ((ResultDockable) dock).getTitleText().substring(7));
					} else {
						if(((ResultDockable)dock).getContent() instanceof ResultGraphPanel)
							((ResultGraphPanel)((ResultDockable)dock).getContent()).tabSelected();
					}
				}
			}
		});
		
		//si type balise, on affiche les infos contextuelles sur cette balise
		if(type.equals("balise")){
			try {
				if(DatabaseManager.getCurrentStip().executeQuery("select * from balises where name = '"+criteria[0]+"'").next()) {
					getInstance().context.showInfo(DatabaseManager.Type.STIP, StipController.BALISES, criteria[0]);
				} 
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		getInstance().setVisible(true);
	}

	/**
	 * Ajoute un tab de résultats et ouvre la fenêtre si besoin
	 * @param type Type de recherche
	 * @param balise Balise cherchée
	 */
	public final static void showResults(String type, String balise){
		showResults(false, type, balise, "", "");
	}

	private AnalyzeUI(){
		super();
		getContentPane().setLayout(new BorderLayout());
		
		this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/videso3d.png")));

		this.setTitle("Videso - Analyse ("+Videso3D.VERSION+")");

		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		getContentPane().add(topPanel, BorderLayout.PAGE_START);

		topPanel.add(searchPanelContainer);
		
		advancedSearch = new JButton("Recherche avancée");
		advancedSearch.setEnabled(false);
		
		searchPanel = new SearchPanel();
		searchPanel.getTypeComboBox().addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
					advancedSearch.setEnabled(isAdvancedSearchAvailable((String) ((JComboBox)e.getSource()).getSelectedItem()));
			}
		});
		this.searchPanelContainer.add(searchPanel, "default");
		
		Box searchButtonBox = Box.createVerticalBox();
		advancedSearch.addActionListener(new ActionListener() {
			
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(((JButton)e.getSource()).getText().equals("Recherche avancée")){
					setAdvancedSearchPanel(searchPanel.getType());
					advancedSearch.setText("Recherche simplifiée");
				} else {
					setDefaultSearch();
				}
			}
		});
		searchButtonBox.add(Box.createVerticalStrut(5));
		searchButtonBox.add(advancedSearch);
		searchButtonBox.add(Box.createVerticalGlue());

		topPanel.add(searchButtonBox);

		getContentPane().add(this.createStatusBar(), BorderLayout.PAGE_END);


		control = new CControl(this);
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		control.putProperty(EclipseThemeExtension.GLASS_FACTORY, null);
		control.putProperty(EclipseTheme.TAB_PAINTER, CGlassEclipseTabPainter.FACTORY);
		((CEclipseTheme)control.intern().getController().getTheme()).intern().setMovingImageFactory(new CMiniPreviewMovingImageFactory(128), Priority.CLIENT);
		((CEclipseTheme)control.intern().getController().getTheme()).intern().setPaint(new CGlassStationPaint(), Priority.CLIENT);
		control.setGroupBehavior(CGroupBehavior.TOPMOST);
		
		control.addMultipleDockableFactory("results", factory);
						
		control.getContentArea().setBorder(null);
		
		splitpane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, context, control.getContentArea());
		splitpane.setOneTouchExpandable(true);
		splitpane.setContinuousLayout(true);

		this.getContentPane().add(splitpane);
		this.pack();
		//FullScreen
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);
	}	

	private void setDefaultSearch(){
		((CardLayout) searchPanelContainer.getLayout()).show(searchPanelContainer, "default");
		topPanel.setPreferredSize(searchPanel.getPreferredSize());
		topPanel.validate();
		advancedSearch.setText("Recherche avancée");
	}
	
	private void setDefaultSearch(String type){
		searchPanel.getTypeComboBox().setSelectedItem(type);
		setDefaultSearch();
	}
	
	private void setAdvancedSearchPanel(String type){
		getAdvancedSearchPanel(type).getTypeComboBox().setSelectedItem(type);
		topPanel.setPreferredSize(getAdvancedSearchPanel(type).getPreferredSize());
		((CardLayout) searchPanelContainer.getLayout()).show(searchPanelContainer, type);
		topPanel.validate();
	}
	
	
	private AdvancedSearchPanel getAdvancedSearchPanel(String type){
		
		ActionListener typeComboBoxListener = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String type = (String) ((JComboBox)e.getSource()).getSelectedItem();
				if(isAdvancedSearchAvailable(type)){
					setAdvancedSearchPanel(type);
				} else {
					setDefaultSearch(type);
				}
			}
		};
		
		if(type.equals("iti")){
			if(itiSearch == null){
				itiSearch = new ItiSearchPanel();
				searchPanelContainer.add(itiSearch, "iti");
				itiSearch.getTypeComboBox().addActionListener(typeComboBoxListener);
			}
			return itiSearch;
		} else if(type.equals("route")){
			return null;
		} else if(type.equals("trajet")){
			return null;
		} else if(type.equals("balise")){
			if(baliseSearch == null){
				
			}
			return null;
		} else if(type.equals("connexion")){
			return null;
		} else if(type.equals("stars")){
			return null;
		}else if(type.equals("liaison privilégiée")){
			return null;
		} else {
			return null;
		}
	}
	
	private boolean isAdvancedSearchAvailable(String type){
		return type.equals("iti");
	}
	
	private ResultPanel createResultPanel(boolean advanced, final String type, final String... criteria){
		if(type.equals("iti")){
			return new ItiPanel(advanced, criteria);
		} else if(type.equals("route")){
			return new RoutePanel(advanced, criteria);
		} else if(type.equals("trajet")){
			return new TrajetPanel(advanced, criteria);
		} else if(type.equals("balise")){
			return new BaliseResultPanel(criteria[0]);
		} else if(type.equals("connexion")){
			return new ConnexPanel(advanced, criteria);
		} else if(type.equals("stars")){
			return new StarPanel(advanced, criteria);
		} else if(type.equals("liaison privilégiée")){
			return new LiaisonPanel(criteria[2]);
		} else if(type.equals("base PLNS...")){
			return new PLNSPanel(criteria[3]);
		}

		return null;
	}

	private JPanel createStatusBar(){
		String versionStip = "";
		String versionStpv = "";
		try {
			Statement st = DatabaseManager.getCurrent(DatabaseManager.Type.Databases);
			ResultSet rs = st.executeQuery("select * from databases where selected = '1' and type = 'STIP'");
			if(rs.next()) versionStip = rs.getString(2);
			rs = st.executeQuery("select * from databases where selected = '1' and type = 'STPV'");
			if(rs.next()) versionStpv = rs.getString(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		JPanel statusBar = new JPanel();
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.X_AXIS));
		JLabel stip = new JLabel("Version Stip : " + versionStip);
		statusBar.add(stip);
		statusBar.add(new JLabel(" | "));
		statusBar.add(new JLabel("Version Stpv : " + versionStpv));

		statusBar.add(new JLabel(" | "));
		statusBar.add(new JLabel("Nombre de résultats : "));
		statusBar.add(nombreResultats);

		return statusBar;
	}
	
	
	public static ContextPanel getContextPanel(){
		return getInstance().context;
	}
	
	public static void updateSearchBoxes(){
		getInstance().searchPanel.updateSearchBoxes();
	}
	
	
	/* ****************************************************************** */
	/* ********************* MultipleCDockables ************************* */
	/* ****************************************************************** */
	
	private class ResultDockable extends DefaultMultipleCDockable{

		private ResultPanel content;
		
		public ResultDockable(MultipleCDockableFactory<?, ?> factory,
				ResultPanel content) {
			super(factory, content);
			add(content);
			this.content = content;
			
		}
		
		public ResultLayout getLayout(){
			return new ResultLayout(getTitleText(), getComponents()[0]);
		}
		
		public ResultPanel getContent(){
			return this.content;
		}
		
	}
	
	private class ResultFactory implements MultipleCDockableFactory<ResultDockable, ResultLayout>{

		@Override
		public ResultLayout create() {
			return new ResultLayout();
		}

		@Override
		public boolean match(ResultDockable dockable, ResultLayout layout) {
			return dockable.getLayout().equals(layout);
		}

		@Override
		public ResultDockable read(ResultLayout layout) {
			return null;
		}

		@Override
		public ResultLayout write(ResultDockable laout) {
			return null;
		}
		
	}
	
	/**
	 * Content of a {@link ResultDockable}
	 * @author Bruno Spyckerelle
	 *
	 */
	private class ResultLayout implements MultipleCDockableLayout{

		private String title;
		private Component content;
		
		public ResultLayout(String title, Component content){
			this.title = title;
			this.content = content;
		}
		
		public ResultLayout() {
			// do nothing
		}

		@Override
		public boolean equals( Object obj ){
			if( this == obj ){
				return true;
			}
			if( obj == null ){
				return false;
			}
			if( getClass() != obj.getClass() ){
				return false;
			}
			ResultLayout other = (ResultLayout) obj;
			return equals( title, other.title ) && 
				equals( content, other.content );
		}
		
		private boolean equals( Object a, Object b ){
			if( a == null ){
				return b == null;
			}
			else{
				return a.equals( b );
			}
		}
		
		@Override
		public void readStream(DataInputStream arg0) throws IOException {}

		@Override
		public void readXML(XElement arg0) {}

		@Override
		public void writeStream(DataOutputStream arg0) throws IOException {}

		@Override
		public void writeXML(XElement arg0) {}
		
	}
	
	private class AllCloseAction extends CDropDownItem<AllCloseAction.Action>{
		/** the control for which this action works */
		private CControl control;

		/**
		 * Creates a new action
		 * @param control the control for which this action will be used
		 */
		public AllCloseAction( CControl control ) {
			super( null );
			if( control == null )
				throw new NullPointerException( "control is null" );

			this.control = control;
			init( new Action() );

			this.setTooltip("Fermer tous les onglets");
			this.setText("Fermer tous les onglets");
		}


		public void close( CDockable dockable ){
			while(control.getCDockableCount() > 0){
				control.getCDockable(0).setVisible(false);
			}
		}

		/**
		 * Inspired by {@link CCloseAction}
		 */
		public class Action extends SimpleButtonAction implements CommonDropDownItem{
			/** how often this action was bound */
			private int count = 0;

			private DockActionIcon icon;

			/**
			 * Creates a new action
			 */
			public Action(){
				this( null );
			}


			public Action( DockController controller ){
				icon = new DockActionIcon( "close", this ){
					protected void changed( Icon oldValue, Icon newValue ){
						setIcon( newValue );	
					}
				};

				setController( controller );
			}

			public void setController( DockController controller ) {
				icon.setController( controller );
			}

			@Override
			public void action( Dockable dockable ) {
				close( dockable );
			}


			protected void close( Dockable dockable ) {
				if( dockable instanceof CommonDockable ){
					AllCloseAction.this.close( ((CommonDockable)dockable).getDockable() );
				}
				else {
					DockStation parent = dockable.getDockParent();
					if( parent != null )
						parent.drag( dockable );
				}
			}

			@Override
			protected void bound( Dockable dockable ) {
				super.bound( dockable );
				if( count == 0 ){
					setController( control.intern().getController() );
				}
				count++;
			}

			@Override
			protected void unbound( Dockable dockable ) {
				super.unbound( dockable );
				count--;
				if( count == 0 ){
					setController( null );
				}
			}

			public CAction getAction(){
				return AllCloseAction.this;
			}
		}
	}
}

