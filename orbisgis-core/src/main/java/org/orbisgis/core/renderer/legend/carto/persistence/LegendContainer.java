//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.10.25 at 04:49:43 PM CEST 
//


package org.orbisgis.core.renderer.legend.carto.persistence;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="legend-description" type="{persistence.carto.legend.renderer.core.orbisgis.org}legend-type"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "legendDescription"
})
@XmlRootElement(name = "legend-container")
public class LegendContainer {

    @XmlElement(name = "legend-description", required = true)
    protected LegendType legendDescription;

    /**
     * Gets the value of the legendDescription property.
     * 
     * @return
     *     possible object is
     *     {@link LegendType }
     *     
     */
    public LegendType getLegendDescription() {
        return legendDescription;
    }

    /**
     * Sets the value of the legendDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link LegendType }
     *     
     */
    public void setLegendDescription(LegendType value) {
        this.legendDescription = value;
    }

}
