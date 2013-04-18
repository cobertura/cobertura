/*
 * Cobertura - http://cobertura.sourceforge.net/
 *
 * Copyright (C) 2007 Joakim Erdfelt
 *
 * Cobertura is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Cobertura is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cobertura; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package net.sourceforge.cobertura.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * A Utility Class to load the configuration.
 * 
 * Checks for values using the following hierarchy.
 * 1) System Property matching key.
 * 2) cobertura.properties Resource Property matching key.
 * 3) hardcoded default value
 * 
 * @author Joakim Erdfelt
 */
public class ConfigurationUtil
{
    public static final String RESOURCE = "/cobertura.properties";

    private Properties props;

    public ConfigurationUtil()
    {
        init();
    }

    public void init()
    {
        props = new Properties();

        URL url = this.getClass().getResource( RESOURCE );
        if ( url == null )
        {
            DEBUG( "Unable to find configuration resource in classpath of name " + RESOURCE + ", using empty configuration." );
            return;
        }

        InputStream is = null;
        try
        {
            is = url.openStream();
            props.load( is );
        }
        catch ( IOException e )
        {
            System.err.println( "ERROR: Unable to load configuration resource " + RESOURCE + " - " + e.getMessage() );
        }
        finally
        {
            IOUtil.closeInputStream( is );
        }
    }

    public String getProperty( String key, String defvalue )
    {
        String value = System.getProperty( key );
        if ( value != null )
        {
            DEBUG("Using system property value [" + value + "] for key [" + key + "]");
            return value;
        }

        value = props.getProperty( key );
        if ( value != null )
        {
            DEBUG("Using cobertura.properties value [" + value + "] for key [" + key + "]");
            return value;
        }

        DEBUG("Using default value [" + defvalue + "] for key [" + key + "]");
        return defvalue;
    }

    public String getDatafile()
    {
        return getProperty( "net.sourceforge.cobertura.datafile", "cobertura.ser" );
    }
    
    /**
     * Poor mans debugging.
     * Intentionally didn't use log4j, as we dont want to introduce that dependency on instrumented files.
     */
    private void DEBUG(String msg)
    {
        if (false) {
            System.out.println("[Cobertura:ConfigurationUtil] " + msg);
        }
    }
}
