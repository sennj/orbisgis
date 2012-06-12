/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.orbisgis.core.renderer.se.common;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import net.opengis.ows._2.*;
import org.orbisgis.core.renderer.se.SeExceptions.InvalidStyle;

/**
 * This class intends to store a description of a {@code Rule}. It is made of
 * lists of title and abstract, and of sets of keywords. This class is support
 * to manage internationalization. As there can be only one title and one
 * abstract per language, we use a {@code HashMap} to manage them. Keywords are
 * stored in a dedicated class.</p>
 * <p>According to 0GC 06-121r9, there shall be at most one title and/or
 * abstract per language. However, they may be many keywords associated to the
 * same language in a {@code Keywords} instance. In a {@code Description}
 * instance, there shall be at most one {@code Keywords} instance associated
 * to an authority.</p>
 * <p>Authorities are defined only considering the URI contained in the {@code
 * codeSpace} attribute of the {@code CodeType} element contained in {@code
 * Keywords}, according to 0GC 06-121r9. As there shall not be more than one
 * {@code Keywords} instance associated to a single authority, we map keywords
 * on this authority, ie only on the {@code URI}. The {@code CodeType} is not
 * considered meaningful in this mapping.
 * @author alexis
 * @see Keywords
 */
public class Description {

    private HashMap<Locale, String> titles;
    private HashMap<Locale, String> abstractTexts;
    private HashMap<URI,Keywords> keywords;

    /**
     * Builds a new, empty, {@code Description}.
     */
    public Description(){
        titles = new HashMap<Locale, String>();
        abstractTexts = new HashMap<Locale, String>();
        keywords = new  HashMap<URI,Keywords>();
    }
    
    /**
     * Builds a new {@code Description} from the given
     * {@code DescriptionType}.
     * @param dt
     */
    public Description(DescriptionType dt) throws InvalidStyle{
        this();
        List<LanguageStringType> tlst = dt.getTitle();
        if(tlst != null){
            for(LanguageStringType l : tlst){
                String lang = l.getLang();
                Locale loc = LocalizedText.forLanguageTag(lang);
                titles.put(loc,l.getValue());
            }
        }
        List<LanguageStringType> dlst = dt.getAbstract();
        if(dlst !=null){
            for(LanguageStringType l : dlst){
                String lang = l.getLang();
                Locale loc = LocalizedText.forLanguageTag(lang);
                abstractTexts.put(loc,l.getValue());
            }
        }
        List<KeywordsType> lkt = dt.getKeywords();
        if(lkt != null){
            for(KeywordsType kt : lkt){
                putKeywordsType(kt);
            }
        }
    }

    private void putKeywordsType(KeywordsType kt) throws InvalidStyle {
        CodeType ct = kt.getType();
        if(ct!=null){
            String sp = ct.getCodeSpace();
            if(sp != null){
                try{
                    keywords.put(new URI(kt.getType().getCodeSpace()),new Keywords(kt));
                } catch (URISyntaxException ex) {
                    throw new InvalidStyle("The provided URI is not valid.", ex);
                }
            } else {
                keywords.put(null,new Keywords(kt));
            }
        } else {
            keywords.put(null,new Keywords(kt));
        }

    }

    /**
     * Gets the list of localized abstracts registered in this {@code
     * Description}.
     * @return
     */
    public HashMap<Locale, String> getAbstractTexts() {
        return abstractTexts;
    }

    /**
     * Gets the list of localized keywords registered in this {@code
     * Description}.
     * @return
     */
    public HashMap<URI,Keywords> getKeywords() {
        return keywords;
    }

    /**
     * Gets the set of keywords associated to this {@code URI}.
     * @param uri
     * @return
     */
    public Keywords getKeywords(URI uri){
        return keywords.get(uri);
    }

    /**
     * Sets the set of keywords associated to this {@code URI}.
     * @param uri
     * @param keys
     */
    public void putKeywords(URI uri, Keywords keys){
        keywords.put(uri, keys);
    }

    /**
     * Removes the set of keywords associated to this {@code URI}.
     * @param uri
     * @return
     * The {@code Keywords} instance that has just been removed from the map
     * of Keywords.
     */
    public Keywords removeKeywords(URI uri){
        return keywords.remove(uri);
    }

    /**
     * Gets the list of localized titles registered in this {@code
     * Description}.
     * @return
     */
    public HashMap<Locale, String> getTitles() {
        return titles;
    }
    
    /**
     * Sets the list of localized titles registered in this {@code
     * Description}.
     * @param titles The map of titles
     */
    public void setTitles(HashMap<Locale,String> titles) {
            this.titles = titles;
    }

    /**
     * Adds a title to this {@code Description}, associated to the given {@code
     * Locale}.
     * @param text
     * @param locale
     * @return
     * The title that was previously associated to {@code Locale}, if any.
     */
    public String addTitle(Locale locale,String text){
        return titles.put(locale, text);
    }

    /**
     * Gets the title of this {@code Description} associated to the given {@code
     * Locale}.
     * @param locale
     * @return
     */
    public String getTitle(Locale locale){
        return titles.get(locale);
    }

    /**
     * Adds an abstract to this {@code Description}, associated to the given
     * {@code Locale}.
     * @param text
     * @param locale
     * @return
     * The title that was previously associated to {@code Locale}, if any.
     */
    public String addAbstract(Locale locale,String text){
        return abstractTexts.put(locale, text);
    }

    /**
     * Gets the abstract of this {@code Description} associated to the given
     * {@code Locale}.
     * @param locale
     * @return
     */
    public String getAbstract(Locale locale){
        return abstractTexts.get(locale);
    }
    
    /**
     * Initialise the data of the provided description type with
     * the JAXB representation of this object.
     * @param[in-out] dt Instance of DescriptionType
     */
    public void initJAXBType(DescriptionType dt) {
        ObjectFactory of = new ObjectFactory();
        List<LanguageStringType> ts = dt.getTitle();
        for(Map.Entry<Locale, String> lt : titles.entrySet()){
            LanguageStringType lst = of.createLanguageStringType();
            lst.setLang(lt.getKey()!= null ? LocalizedText.toLanguageTag(lt.getKey()) : "");
            lst.setValue(lt.getValue());
            ts.add(lst);
        }
        List<LanguageStringType> abs = dt.getAbstract();
        for(Map.Entry<Locale, String> lt : abstractTexts.entrySet()){
            LanguageStringType lst = of.createLanguageStringType();
            lst.setLang(lt.getKey()!= null ? LocalizedText.toLanguageTag(lt.getKey()) : "");
            lst.setValue(lt.getValue());
            abs.add(lst);
        }
        List<KeywordsType> kts = dt.getKeywords();
        Set<Map.Entry<URI, Keywords>> registered = keywords.entrySet();
        for(Map.Entry<URI, Keywords> entry : registered){
            KeywordsType kwjt = entry.getValue().getJAXBType();
            if(entry.getKey() != null && entry.getValue().getType() != null){
                kwjt.getType().setCodeSpace(entry.getKey().toString());
            }
            kts.add(kwjt);
        }            
    }
    
    /**
     * Gets the JAXB representation of this object.
     * @return
     */
    public DescriptionType getJAXBType() {
        ObjectFactory of = new ObjectFactory();
        DescriptionType dt = of.createDescriptionType();
        initJAXBType(dt);
        return dt;
    }
    
}