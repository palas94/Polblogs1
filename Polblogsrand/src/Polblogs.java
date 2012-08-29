import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.util.* ;
import java.awt.geom.Rectangle2D;
import java.io.* ;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;


import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataShapeAction;
import prefuse.action.distortion.Distortion;
import prefuse.action.distortion.FisheyeDistortion;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.BalloonTreeLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.action.layout.graph.FruchtermanReingoldLayout;
import prefuse.action.layout.graph.NodeLinkTreeLayout;
import prefuse.action.layout.graph.RadialTreeLayout;
import prefuse.activity.Activity;
import prefuse.controls.AnchorUpdateControl;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.PolygonRenderer;
import prefuse.render.Renderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.GraphicsLib;
import prefuse.util.force.CircularWallForce;
import prefuse.util.force.DragForce;
import prefuse.util.force.Force;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.GravitationalForce;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.RungeKuttaIntegrator;
import prefuse.util.force.SpringForce;
import prefuse.util.ui.JFastLabel;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;




public class Polblogs extends Display {
	
double y, lc, cc, nc, i1c , i2c ;

	 public static final String GRAPH = "graph";
	    public static final String NODES = "graph.nodes";
	    public static final String EDGES = "graph.edges";
	    public static final String AGGR = "aggregates";

	    public Polblogs() throws FileNotFoundException {
	    	 super(new Visualization());
	         initDataGroups();
	         
	      // set up the renderers
	         // draw the nodes as basic shapes
	         // draw the "name" label for NodeItems
	         ShapeRenderer sr = new ShapeRenderer()  ; 
	          Renderer nodeR = sr ;
	         
	         
	         EdgeRenderer edgeR = new EdgeRenderer(prefuse.Constants.EDGE_TYPE_LINE,prefuse.Constants.EDGE_ARROW_FORWARD);
	         
	         
	         // draw aggregates as polygons with curved edges
	         Renderer polyR = new PolygonRenderer(Constants.POLY_TYPE_CURVE);
	         ((PolygonRenderer)polyR).setCurveSlack(0.15f);
	         
	         DefaultRendererFactory drf = new DefaultRendererFactory();
	         drf.setDefaultRenderer(nodeR);
	         drf.setDefaultEdgeRenderer(edgeR) ;
	         drf.add("ingroup('aggregates')", polyR);
	         m_vis.setRendererFactory(drf);

	      // set up the visual operators
	         // first set up all the color actions
	         int[] palette0 = new int[] {
	                 ColorLib.rgb(255,200,200), ColorLib.rgb(200,255,200)
	             };
	             // map nominal data values to colors using our provided palette
	             DataColorAction fill = new DataColorAction("graph.nodes", "value",
	                     Constants.NOMINAL, VisualItem.FILLCOLOR, palette0);
	         
	         ColorAction nStroke = new ColorAction(NODES, VisualItem.STROKECOLOR);
	         nStroke.setDefaultColor(ColorLib.gray(100));
	         nStroke.add("_hover", ColorLib.gray(50));
	         
	         
	         ColorAction nEdges = new ColorAction(EDGES, VisualItem.STROKECOLOR);
	         nEdges.setDefaultColor(ColorLib.rgba(0,245,255,200));
	         
	       

	         
	         ColorAction aStroke = new ColorAction(AGGR, VisualItem.STROKECOLOR);
	         
	         aStroke.add("_hover", ColorLib.rgb(255,100,100));
	         
	         int[] palette = new int[] {
	             ColorLib.rgba(255,200,200,100),
	             ColorLib.rgba(200,255,200,100),
	             ColorLib.rgba(200,200,255,150)
	         };
	         ColorAction aFill = new DataColorAction(AGGR, "value",
	                 Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
	         
	         ColorAction text = new ColorAction("graph.nodes",
	                 VisualItem.TEXTCOLOR, ColorLib.gray(0));
	         
	         
	         // bundle the color actions
	         ActionList colors = new ActionList();
	         colors.add(nStroke);
	         colors.add(nEdges);
	         
	         
	         colors.add(fill) ;
	         colors.add(text) ;
	       
	         
	        
	         ForceSimulator sim = new ForceSimulator(new RungeKuttaIntegrator());
	         sim.addForce(new NBodyForce(-0.1f, 10f, 0.899f));
	         sim.addForce(new DragForce());
	         sim.addForce(new GravitationalForce(prefuse.util.force.GravitationalForce.DEFAULT_FORCE_CONSTANT , prefuse.util.force.GravitationalForce.DEFAULT_MIN_DIRECTION));
		        
	        
	         
	         
	         
	         
	         // now create the main layout routine
	         ActionList layout = new ActionList(Activity.INFINITY);
	         layout.add(colors);
	         layout.add(new ForceDirectedLayout(GRAPH, sim, false));
	         layout.add(new AggregateLayout(AGGR));
	         layout.add(new RepaintAction());
	        
	         m_vis.putAction("layout", layout);
	         
	         ToolTipControl ttc = new ToolTipControl("label");
	         
	         
	         // set up the display
	         setSize(1280,800);
	         pan(250, 250);
	         setHighQuality(true);
	         addControlListener(new AggregateDragControl());
	         addControlListener(new WheelZoomControl());
	         addControlListener(new PanControl());
	         addControlListener(new FinalControlListener()) ;
	         addControlListener(ttc);
	         setBackgroundImage("/Users/Apple/Pictures/1280_Graph Paper.jpeg" , true, true) ;
	         
	      
	         // set things running
	         m_vis.run("layout");
	         
	        
	        
	         
	         
	     }
	    
	    private void initDataGroups() throws FileNotFoundException {
	    	Graph graph = new Graph(true) ;
	        
	        graph.addColumn("id", Integer.class);
	        graph.addColumn("label", String.class);
	        graph.addColumn("value", String.class);
	        graph.addColumn("source", String.class);

	        Scanner c = new Scanner(new File("/Users/Apple/Desktop/Polblogsnode.txt")); 
	      
	        String k[] = new String[1490] ;
	        int x = 0 ;
	        y = 0 ;
	        lc = 0 ;
	        cc = 0 ;
	        nc = 0 ;
	        i1c = 0 ;
	        i2c = 0 ;
	        
	        while (c.hasNextLine()) 
	        {
	        	Node n = graph.addNode();
	        	c.nextLine();
	        	
	        	
	        	Scanner sc1 = new Scanner(c.nextLine()) ;
	        	sc1.useDelimiter("id");
	        	sc1.next();
	        	Integer iddata = Integer.parseInt((sc1.next()).trim());
	        	n.set("id", iddata) ;
	        	
	        	Scanner sc2 = new Scanner(c.nextLine()) ;
	        	sc2.useDelimiter("label");
	        	sc2.next();
	        	String labeldata = (sc2.next()).trim();
	        	n.set("label", labeldata) ;
	        	
	        	Scanner sc3 = new Scanner(c.nextLine()) ;
	        	sc3.useDelimiter("value");
	        	sc3.next();
	        	String valuedata = (sc3.next()).trim();
	        	n.set("value", valuedata) ;
	        	
	        	Scanner sc4 = new Scanner(c.nextLine()) ;
	        	sc4.useDelimiter("source");
	        	sc4.next();
	        	String sourcedata = (sc4.next()).trim();
	        	n.set("source", sourcedata) ;
	        	
	        	c.nextLine();
	        	
	        	k[x] = valuedata ;
	        			x++ ;
	        	
	        }
	        
	        int b[][]=new int[19090][2];

	        	
	        int rcount = 0 ;
            
            while (rcount < 19090) {
            Random rand = new Random();
            int first = rand.nextInt(1490) ;
            int second = rand.nextInt(1490) ;
            if (first != second){
            graph.addEdge(first, second);
            rcount++ ;
            b[(int) y][0]=first;
        	b[(int) y][1]=second;
        	y++ ;
        	if (k [first ] .equals (k [second ])) {
        		if (k[first].equals("0"))
        		{
        			lc++ ;
        		}
        		if (k[first].equals("1"))
        		{
        			cc++ ;
        		}
}
    		if (k[first].equals("0") && k[second].equals("1"))
    		{
    			i1c ++ ;
    		}
    		else if (k[first].equals("1") && k[second].equals("0"))
    		{
    			i2c ++ ;
        	}
            }
            }
            
	            
	          

	             System.out.println("The ratio of edges between nodes of liberal inclination and the total number of edges is " + (lc/y) );
	            System.out.println("The ratio of edges between nodes of conservative inclination and the total number of edges is " + (cc/y) );
	            System.out.println(lc + " " + cc) ;
	            
	            int c1 = 0, c2 = 0 ;
		        
		           for (int i = 0 ; i < 1490 ;i++)
		           {
		        	   String xs =  (String) (graph.getNode(i)).get("value") ;
		        	    if (xs.equals("0"))
		        	    {
		        	    	c1++ ;
		        	    }
		        	    else c2++ ;
		        	    
		        	   
		           }
		           
		           System.out.println (c1 + " " + c2) ;
		            
	            
	           /* int Max = graph.getNode(0).getInDegree() ;
	            
	            int temp = 0 ;
	            for (int i = 0 ; i < 1490 ;i++)
	            {
	            	if ( (graph.getNode(i)).getInDegree() < Max)
	            	{
	            		Max =  graph.getNode(i).getInDegree() ;
	            		temp = i ;
	            	}
	            }
	           
	           System.out.println(temp + " " + Max) ;
	           */
	           
		           int cluscout = 0 ;
		            
		            for ( int i = 0 ; i < 1490 ; i++) 
		            {
		            	Iterator mal1 = graph.getNode(i).outNeighbors() ;
		            	while (mal1.hasNext())
		            	{
		            		Node n = (Node) mal1.next();
		            		int ops = (Integer) n.get("id") ;
		            		Iterator mal2 = graph.getNode(ops - 1).outNeighbors() ;
		            		while (mal2.hasNext())
			            	{
			            		Node n2 = (Node) mal2.next();
			            		int ops2 = (Integer) n2.get("id") ;
			            		Iterator mal3 = graph.getNode(ops2 - 1).outNeighbors() ;
			            		while (mal3.hasNext())
				            	{
				            		Node n3 = (Node) mal3.next();
				            		int ops3 = (Integer) n3.get("id") ;
				            		int ops4 = (Integer) graph.getNode(i).get("id") ;
				            		if (ops3 == ops4)
				            		{
				            			cluscout++ ;
				            			break;
				            		}
		            	}
		            		
		            	}
		            	}
		            	
		            	
		            }
		           
		            System.out.println("clus"+cluscout) ;
	            
	            
	            VisualGraph vg = m_vis.addGraph(GRAPH, graph);
	            m_vis.setInteractive(EDGES, null, false);
	            m_vis.setValue(NODES, null, VisualItem.SHAPE,
	                    new Integer(Constants.SHAPE_RECTANGLE)); //change shape here
	            
	            AggregateTable at = m_vis.addAggregates(AGGR);
	            at.addColumn(VisualItem.POLYGON, float[].class);
	            at.addColumn("value", int.class);
	            
	            Iterator mal1 = m_vis.items(NODES, ExpressionParser.predicate("value = '0'"));
	            Iterator mal2 = m_vis.items(NODES, ExpressionParser.predicate("value = '1'"));
	            Iterator mal3 = m_vis.items(NODES, ExpressionParser.predicate("value = '2'"));
	            
	            for (int i = 0; i < 3; i++) 
	            {
	            	AggregateItem aitem = (AggregateItem)at.addItem();
	            	aitem.setInt("value", i); 
	            	
	            	if(i==0){
	            		while(mal1.hasNext())

	              	  {

	              		  aitem.addItem((VisualItem)mal1.next());

	              	  }
	            	}
	            	
	            	
	            	if(i==1){
	            		while(mal2.hasNext())

	              	  {

	              		  aitem.addItem((VisualItem)mal2.next());

	              	  }
	            	}
	            	
	            	if(i==2){
	            		while(mal3.hasNext())

	              	  {

	              		  aitem.addItem((VisualItem)mal3.next());

	              	  }
	            	}
	            }
	    }
	    
	    
	    
	    
	    public static void main(String[] argv) throws FileNotFoundException {
	        JFrame frame = demo();
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        frame.setVisible(false);
	    
	       
	    }
	    
	    public static JFrame demo() throws FileNotFoundException {
	        Polblogs ad = new Polblogs();
	        JFrame frame = new JFrame("Assignment 1");
	        frame.getContentPane().add(ad);
	        frame.pack();
	        return frame;
	    }
	    
	
}


class AggregateLayout extends Layout {
    
    private int m_margin = 5; // convex hull pixel margin
    private double[] m_pts;   // buffer for computing convex hulls
    
    public AggregateLayout(String aggrGroup) {
        super(aggrGroup);
    }
    
    /**
     * @see edu.berkeley.guir.prefuse.action.Action#run(edu.berkeley.guir.prefuse.ItemRegistry, double)
     */
    public void run(double frac) {
        
        AggregateTable aggr = (AggregateTable)m_vis.getGroup(m_group);
        // do we have any  to process?
        int num = aggr.getTupleCount();
        if ( num == 0 ) return;
        
        // update buffers
        int maxsz = 0;
        for ( Iterator aggrs = aggr.tuples(); aggrs.hasNext();  )
            maxsz = Math.max(maxsz, 4*2*
                    ((AggregateItem)aggrs.next()).getAggregateSize());
        if ( m_pts == null || maxsz > m_pts.length ) {
            m_pts = new double[maxsz];
        }
        
        // compute and assign convex hull for each aggregate
        Iterator aggrs = m_vis.visibleItems(m_group);
        while ( aggrs.hasNext() ) {
            AggregateItem aitem = (AggregateItem)aggrs.next();

            int idx = 0;
            if ( aitem.getAggregateSize() == 0 ) continue;
            VisualItem item = null;
            Iterator iter = aitem.items();
            while ( iter.hasNext() ) {
                item = (VisualItem)iter.next();
                if ( item.isVisible() ) {
                    addPoint(m_pts, idx, item, m_margin);
                    idx += 2*4;
                }
            }
            // if no aggregates are visible, do nothing
            if ( idx == 0 ) continue;

            // compute convex hull
            double[] nhull = GraphicsLib.convexHull(m_pts, idx);
            
            // prepare viz attribute array
            float[]  fhull = (float[])aitem.get(VisualItem.POLYGON);
            if ( fhull == null || fhull.length < nhull.length )
                fhull = new float[nhull.length];
            else if ( fhull.length > nhull.length )
                fhull[nhull.length] = Float.NaN;
            
            // copy hull values
            for ( int j=0; j<nhull.length; j++ )
                fhull[j] = (float)nhull[j];
            aitem.set(VisualItem.POLYGON, fhull);
            aitem.setValidated(false); // force invalidation
        }
    }
    
    private static void addPoint(double[] pts, int idx, 
                                 VisualItem item, int growth)
    {
        Rectangle2D b = item.getBounds();
        double minX = (b.getMinX())-growth, minY = (b.getMinY())-growth;
        double maxX = (b.getMaxX())+growth, maxY = (b.getMaxY())+growth;
        pts[idx]   = minX; pts[idx+1] = minY;
        pts[idx+2] = minX; pts[idx+3] = maxY;
        pts[idx+4] = maxX; pts[idx+5] = minY;
        pts[idx+6] = maxX; pts[idx+7] = maxY;
    }
   
}


class AggregateDragControl extends ControlAdapter {

    private VisualItem activeItem;
    protected Point2D down = new Point2D.Double();
    protected Point2D temp = new Point2D.Double();
    protected boolean dragged;
    
    /**
     * Creates a new drag control that issues repaint requests as an item
     * is dragged.
     */
    public AggregateDragControl() {
    }
        
    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemEntered(VisualItem item, MouseEvent e) {
        Display d = (Display)e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        activeItem = item;
        if ( !(item instanceof AggregateItem) )
            setFixed(item, true);
    }
    
    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemExited(VisualItem item, MouseEvent e) {
        if ( activeItem == item ) {
            activeItem = null;
            setFixed(item, false);
        }
        Display d = (Display)e.getSource();
        d.setCursor(Cursor.getDefaultCursor());
    }
    
    /**
     * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemPressed(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        dragged = false;
        Display d = (Display)e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), down);
        if ( item instanceof AggregateItem )
            setFixed(item, true);
    }
    
    /**
     * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemReleased(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        if ( dragged ) {
            activeItem = null;
            setFixed(item, false);
            dragged = false;
        }            
    }
    
    /**
     * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemDragged(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        dragged = true;
        Display d = (Display)e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), temp);
        double dx = temp.getX()-down.getX();
        double dy = temp.getY()-down.getY();
        
        move(item, dx, dy);
        
        down.setLocation(temp);
    }

    protected static void setFixed(VisualItem item, boolean fixed) {
        if ( item instanceof AggregateItem ) {
            Iterator items = ((AggregateItem)item).items();
            while ( items.hasNext() ) {
                setFixed((VisualItem)items.next(), fixed);
            }
        } else {
            item.setFixed(fixed);
        }
    }
    
    protected static void move(VisualItem item, double dx, double dy) {
        if ( item instanceof AggregateItem ) {
            Iterator items = ((AggregateItem)item).items();
            while ( items.hasNext() ) {
                move((VisualItem)items.next(), dx, dy);
            }
        } else {
            double x = item.getX();
            double y = item.getY();
            item.setStartX(x);  item.setStartY(y);
            item.setX(x+dx);    item.setY(y+dy);
            item.setEndX(x+dx); item.setEndY(y+dy);
        }
    }
    
} // end of class AggregateDragControl

	    
