package Armadillo.Core.Math;

import org.joda.time.DateTime;

public interface ITsEvent {
    DateTime getTime();
    void setTime(DateTime dateTime);

    //[XmlIgnore]
    //[Browsable(false)]
    //TsDataRequest TsDataRequest { get; set; }
    String ToCsvString();
    Object GetHardPropertyValue(String strFieldName);

}
