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
package fr.crnan.videso3d.formats.plns;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.plot.CategoryPlot;

import fr.crnan.videso3d.databases.DatabaseManager.Type;
import fr.crnan.videso3d.databases.stpv.StpvController;
import fr.crnan.videso3d.ihm.ContextPanel;
/**
 * 
 * @author Bruno Spyckerelle
 * @version 0.1.0
 */
public class PLNSChartMouseListener implements ChartMouseListener {

	private ContextPanel context;
	
	public PLNSChartMouseListener(ContextPanel context){
		this.context = context;
	}
	
	@Override
	public void chartMouseClicked(ChartMouseEvent evt) {
		if(evt.getEntity() instanceof CategoryItemEntity){
			CategoryItemEntity entity = (CategoryItemEntity) evt.getEntity();
			//try to guess the type of the entity
			if(entity.getColumnKey() instanceof String && ((String)entity.getColumnKey()).matches("C.")){
				this.context.showInfo(Type.STPV, StpvController.CATEGORIE_CODE, (String)entity.getColumnKey());
			} else if(entity.getColumnKey() instanceof Integer && evt.getChart().getPlot() instanceof CategoryPlot && 
					((CategoryPlot)evt.getChart().getPlot()).getDomainAxis().getLabel().equals("LP")){
				this.context.showInfo(Type.STPV, StpvController.LIAISON_PRIVILEGIEE, ((Integer)entity.getColumnKey()).toString());			
			}
		} else if(evt.getEntity() instanceof CategoryLabelEntity){
			CategoryLabelEntity entity = (CategoryLabelEntity) evt.getEntity();
			if(entity.getKey() instanceof String && ((String)entity.getKey()).matches("C.")){
				this.context.showInfo(Type.STPV, StpvController.CATEGORIE_CODE, (String)entity.getKey());
			} else if(entity.getKey() instanceof Integer && evt.getChart().getPlot() instanceof CategoryPlot && 
					((CategoryPlot)evt.getChart().getPlot()).getDomainAxis().getLabel().equals("LP")){
				this.context.showInfo(Type.STPV, StpvController.LIAISON_PRIVILEGIEE, ((Integer)entity.getKey()).toString());			
			}
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent evt) {
		
	}

}
