//package Web.Table;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.el.ValueExpression;
//import javax.faces.context.FacesContext;
//
//import org.primefaces.component.datatable.DataTable;
//
///**
// * Extending PF data table to allow for binding of the filter map.
// */
//public class MyDataTable extends DataTable 
//{
//
//   /**
//    * Locates the filterMap attribute on the datatable.
//    * <p>
//    * @return the ValueExpression provided in the filterMap attribute if
//    *         specified or null if not
//    */
//   protected ValueExpression getFilterFacetValueExpression() {
//	   
//      ValueExpression ve = getValueExpression("filterMap");
//      return ve;
//   }
//
//   /*
//    * (non-Javadoc)
//    * @see org.primefaces.component.datatable.DataTable#getFilters()
//    */
//   @Override
//   public java.util.Map<String, String> getFilters() {
//      ValueExpression ve = getFilterFacetValueExpression();
//      if (ve == null)
//         return super.getFilters();
//
//      @SuppressWarnings("unchecked")
//	Map<String, String> map = (Map<String, String>) ve.getValue(FacesContext.getCurrentInstance().getELContext());
//      //LOG.trace("Facet found and map is {}", map);
//      if (map == null)
//         return new HashMap<String, String>();
//      else
//         return map;
//   };
//
//   /*
//    * (non-Javadoc)
//    * @see
//    * org.primefaces.component.datatable.DataTable#setFilters(java.util.Map)
//    */
//   @Override
//   public void setFilters(java.util.Map<String, String> filters) {
//      ValueExpression ve = getFilterFacetValueExpression();
//      if (ve == null) {
//         super.setFilters(filters);
//         return;
//      }
//
//      ve.setValue(FacesContext.getCurrentInstance().getELContext(), filters);
//      //LOG.trace("Facet updated to {}", filters);
//   };
//}