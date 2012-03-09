//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.12.02 at 02:08:08 PM CET 
//


package net.opengis.se._2_0.core;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import net.opengis.fes._2.LiteralType;


/**
 * <p>Java class for CategorizeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="CategorizeType">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.opengis.net/se/2.0/core}FunctionType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.opengis.net/se/2.0/core}LookupValue"/>
 *         &lt;sequence maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{http://www.opengis.net/se/2.0/core}Threshold"/>
 *           &lt;element ref="{http://www.opengis.net/se/2.0/core}Value"/>
 *         &lt;/sequence>
 *         &lt;element ref="{http://www.opengis.net/se/2.0/core}Extension" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="thresholdBelongsTo" type="{http://www.opengis.net/se/2.0/core}ThresholdBelongsToType" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CategorizeType", propOrder = {
    "lookupValue",
    "thresholdAndValue",
    "extension"
})
public class CategorizeType
    extends FunctionType
{

    @XmlElement(name = "LookupValue", required = true)
    protected ParameterValueType lookupValue;
    @XmlElements({
        @XmlElement(name = "Threshold", type = LiteralType.class),
        @XmlElement(name = "Value", type = ParameterValueType.class)
    })
    protected List<Object> thresholdAndValue;
    @XmlElement(name = "Extension")
    protected ExtensionType extension;
    @XmlAttribute(name = "thresholdBelongsTo")
    protected ThresholdBelongsToType thresholdBelongsTo;

    /**
     * Gets the value of the lookupValue property.
     * 
     * @return
     *     possible object is
     *     {@link ParameterValueType }
     *     
     */
    public ParameterValueType getLookupValue() {
        return lookupValue;
    }

    /**
     * Sets the value of the lookupValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParameterValueType }
     *     
     */
    public void setLookupValue(ParameterValueType value) {
        this.lookupValue = value;
    }

    /**
     * Gets the value of the thresholdAndValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the thresholdAndValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getThresholdAndValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LiteralType }
     * {@link ParameterValueType }
     * 
     * 
     */
    public List<Object> getThresholdAndValue() {
        if (thresholdAndValue == null) {
            thresholdAndValue = new ArrayList<Object>();
        }
        return this.thresholdAndValue;
    }

    /**
     * Gets the value of the extension property.
     * 
     * @return
     *     possible object is
     *     {@link ExtensionType }
     *     
     */
    public ExtensionType getExtension() {
        return extension;
    }

    /**
     * Sets the value of the extension property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtensionType }
     *     
     */
    public void setExtension(ExtensionType value) {
        this.extension = value;
    }

    /**
     * Gets the value of the thresholdBelongsTo property.
     * 
     * @return
     *     possible object is
     *     {@link ThresholdBelongsToType }
     *     
     */
    public ThresholdBelongsToType getThresholdBelongsTo() {
        return thresholdBelongsTo;
    }

    /**
     * Sets the value of the thresholdBelongsTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThresholdBelongsToType }
     *     
     */
    public void setThresholdBelongsTo(ThresholdBelongsToType value) {
        this.thresholdBelongsTo = value;
    }

}