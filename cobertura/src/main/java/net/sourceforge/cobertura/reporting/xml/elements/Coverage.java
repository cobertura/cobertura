//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.06.21 at 09:57:12 PM EDT 
//


package net.sourceforge.cobertura.reporting.xml.elements;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{http://cobertura.sourceforge.net/xml/}sources" minOccurs="0"/>
 *         &lt;element ref="{http://cobertura.sourceforge.net/xml/}packages"/>
 *       &lt;/sequence>
 *       &lt;attribute name="line-rate" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="branch-rate" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lines-covered" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="lines-valid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="branches-covered" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="branches-valid" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="complexity" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="timestamp" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sources",
    "packages"
})
@XmlRootElement(name = "coverage")
public class Coverage {

    protected Sources sources;
    @XmlElement(required = true)
    protected Packages packages;
    @XmlAttribute(name = "line-rate", required = true)
    protected String lineRate;
    @XmlAttribute(name = "branch-rate", required = true)
    protected String branchRate;
    @XmlAttribute(name = "lines-covered", required = true)
    protected String linesCovered;
    @XmlAttribute(name = "lines-valid", required = true)
    protected String linesValid;
    @XmlAttribute(name = "branches-covered", required = true)
    protected String branchesCovered;
    @XmlAttribute(name = "branches-valid", required = true)
    protected String branchesValid;
    @XmlAttribute(name = "complexity", required = true)
    protected String complexity;
    @XmlAttribute(name = "version", required = true)
    protected String version;
    @XmlAttribute(name = "timestamp", required = true)
    protected String timestamp;

    /**
     * Gets the value of the sources property.
     * 
     * @return
     *     possible object is
     *     {@link Sources }
     *     
     */
    public Sources getSources() {
        return sources;
    }

    /**
     * Sets the value of the sources property.
     * 
     * @param value
     *     allowed object is
     *     {@link Sources }
     *     
     */
    public void setSources(Sources value) {
        this.sources = value;
    }

    /**
     * Gets the value of the packages property.
     * 
     * @return
     *     possible object is
     *     {@link Packages }
     *     
     */
    public Packages getPackages() {
        return packages;
    }

    /**
     * Sets the value of the packages property.
     * 
     * @param value
     *     allowed object is
     *     {@link Packages }
     *     
     */
    public void setPackages(Packages value) {
        this.packages = value;
    }

    /**
     * Gets the value of the lineRate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLineRate() {
        return lineRate;
    }

    /**
     * Sets the value of the lineRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLineRate(String value) {
        this.lineRate = value;
    }

    /**
     * Gets the value of the branchRate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBranchRate() {
        return branchRate;
    }

    /**
     * Sets the value of the branchRate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBranchRate(String value) {
        this.branchRate = value;
    }

    /**
     * Gets the value of the linesCovered property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinesCovered() {
        return linesCovered;
    }

    /**
     * Sets the value of the linesCovered property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinesCovered(String value) {
        this.linesCovered = value;
    }

    /**
     * Gets the value of the linesValid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLinesValid() {
        return linesValid;
    }

    /**
     * Sets the value of the linesValid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinesValid(String value) {
        this.linesValid = value;
    }

    /**
     * Gets the value of the branchesCovered property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBranchesCovered() {
        return branchesCovered;
    }

    /**
     * Sets the value of the branchesCovered property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBranchesCovered(String value) {
        this.branchesCovered = value;
    }

    /**
     * Gets the value of the branchesValid property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBranchesValid() {
        return branchesValid;
    }

    /**
     * Sets the value of the branchesValid property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBranchesValid(String value) {
        this.branchesValid = value;
    }

    /**
     * Gets the value of the complexity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComplexity() {
        return complexity;
    }

    /**
     * Sets the value of the complexity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComplexity(String value) {
        this.complexity = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the timestamp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the value of the timestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTimestamp(String value) {
        this.timestamp = value;
    }

}