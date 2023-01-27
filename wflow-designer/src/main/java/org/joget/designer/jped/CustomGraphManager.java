package org.joget.designer.jped;

import java.util.Map;
import org.enhydra.jawe.components.graph.Graph;
import org.enhydra.jawe.components.graph.GraphEAConstants;
import org.enhydra.jawe.components.graph.GraphManager;
import org.enhydra.jawe.components.graph.GraphTransitionInterface;
import org.enhydra.jawe.components.graph.GraphUtilities;
import org.enhydra.jawe.components.graph.NoRouting;
import org.enhydra.shark.xpdl.elements.Transition;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.GraphConstants;

public class CustomGraphManager extends GraphManager {

   public CustomGraphManager(Graph g) {
       super(g);
   }

   @Override
   protected void updateStyle (GraphTransitionInterface gtra,Map propertyMap) {
      String style=GraphUtilities.getStyle((Transition)gtra.getUserObject());
      AttributeMap map = (AttributeMap)propertyMap.get(gtra);
      if (map==null) {
         map = new AttributeMap(gtra.getAttributes());
         propertyMap.put(gtra, map);
      }

      if (style.equals(GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_NO_ROUTING_BEZIER)) {
          // fix for bezier no routing
         GraphConstants.setRouting(map, GraphConstants.ROUTING_DEFAULT);
         GraphConstants.setLineStyle(map,GraphConstants.STYLE_BEZIER);
      } else if (style.equals(GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_NO_ROUTING_SPLINE)) {
          // fix for spline no routing
         GraphConstants.setRouting(map, GraphConstants.ROUTING_DEFAULT);
         GraphConstants.setLineStyle(map,GraphConstants.STYLE_SPLINE);
      } else if (style.equals(GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_SIMPLE_ROUTING_BEZIER)) {
         GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
         GraphConstants.setLineStyle(map,GraphConstants.STYLE_BEZIER);
      } else if (style.equals(GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_SIMPLE_ROUTING_ORTHOGONAL)) {
         GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
         GraphConstants.setLineStyle(map,GraphConstants.STYLE_ORTHOGONAL);
      } else if (style.equals(GraphEAConstants.EA_JAWE_GRAPH_TRANSITION_STYLE_VALUE_SIMPLE_ROUTING_SPLINE)) {
         GraphConstants.setRouting(map, GraphConstants.ROUTING_SIMPLE);
         GraphConstants.setLineStyle(map,GraphConstants.STYLE_SPLINE);
      } else {
         GraphConstants.setRouting(map,new NoRouting());
         GraphConstants.setLineStyle(map,GraphConstants.STYLE_ORTHOGONAL);
      }
   }

}
