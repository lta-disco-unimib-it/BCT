/**
* Copyright 2005 Victor Ferrer
* 
* This file is part of FreeSudoku.
* 
* FreeSudoku is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
* 
* FreeSudoku is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with FreeSudoku; if not, write to the Free Software
* Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA*
*/
package org.gos.freesudoku.view;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

import org.gos.freesudoku.CONSTS;

public class Trans
{
    static final String langIds[] =
    { "ct", "de", "en", "es", "ru" };  // Language code in alphabethical order 
    static final String langDesc[] =
    { "Català", "Deutsch", "English", "Español", getCyrilicISO("\u00c0\u00e3\u00e1\u00e1\u00da\u00d8\u00d9") };  // Language description
    
    private int     currentLang = -1;
    private Hashtable tagsTable = null;
    private static Trans transInst     = null;
    
    private void initTagsTable()
    {
        tagsTable = new Hashtable();
//        
        tagsTable.put( "incorrect_number", new String[]       // Tag Id
                                                      { "Número incorrecte",  // ct 
                                                        "",   // de
                                                        "Incorrect number",   // en
                                                        "Número incorrecto",  // es
                                                        getCyrilicISO("???????????? ?????")}); // ru
        tagsTable.put( "language", new String[]       // Tag Id
                                                      { "Idioma",  // ct 
                                                        "Sprache",   // de
                                                        "Language",   // en
                                                        "Idioma",  // es
                                                        getCyrilicISO("\u00cf\u00d7\u00eb\u00da")}); // ru
        tagsTable.put( "congratulations", new String[]      // Tag Id
                                      { "Felicitats!",      // ct 
                                        "!", // de
                                        "Congratulations!", // en
                                        "Felicidades",      // es
                                        ""});               // ru
        tagsTable.put( "file", new String[]         // Tag Id
                                      { "Arxiu",     // ct 
                                        "",         // de
                                        "File",     // en
                                        "Archivo",     // es
                                        getCyrilicISO("\u00c4\u00d0\u00d9\u00db")});   // ru
        tagsTable.put( "new_game", new String[]         // Tag Id
                                      { "Jugar",     // ct 
                                        "",         // de
                                        "New Game",     // en
                                        "Jugar",     // es
                                        ""});   // ru
        tagsTable.put( "open", new String[]         // Tag Id
                                      { "Obrir",     // ct 
                                        "",         // de
                                        "Open",     // en
                                        "Abrir",     // es
                                        ""});   // ru
        tagsTable.put( "save", new String[]         // Tag Id
                                      { "Guardar",     // ct 
                                        "",         // de
                                        "Save",     // en
                                        "Guardar",     // es
                                        ""});   // ru
        tagsTable.put( "save_as", new String[]         // Tag Id
                                      { "Guardar com...",     // ct 
                                        "",         // de
                                        "Save as...",     // en
                                        "Guardar como...",     // es
                                        ""});   // ru
        tagsTable.put( "exit", new String[]         // Tag Id
                                      { "Sortir",     // ct 
                                        "",         // de
                                        "Exit",     // en
                                        "Salir",     // es
                                        ""});   // ru
        tagsTable.put( "options", new String[]         // Tag Id
                                      { "Opcions",     // ct 
                                        "",         // de
                                        "Options",     // en
                                        "Opciones",     // es
                                        ""});   // ru
        tagsTable.put( "help", new String[]         // Tag Id
                                      { "Ajuda",     // ct 
                                        "",         // de
                                        "Help",     // en
                                        "Ayuda",     // es
                                        ""});   // ru
        tagsTable.put( "contents", new String[]         // Tag Id
                                      { "Contingut",     // ct 
                                        "",         // de
                                        "Contents",     // en
                                        "Contenido",     // es
                                        ""});   // ru
        tagsTable.put( "about", new String[]         // Tag Id
                                      { "",     // ct 
                                        "",         // de
                                        "About",     // en
                                        "Acerca de",     // es
                                        ""});   // ru
        tagsTable.put( "start", new String[]         // Tag Id
                                      { "Start",     // ct 
                                        "Start",         // de
                                        "Start",     // en
                                        "Start",     // es
                                        "Start"});   // ru
        tagsTable.put( "stop", new String[]         // Tag Id
                                      { "Stop",     // ct 
                                        "Stop",         // de
                                        "Stop",     // en
                                        "Stop",     // es
                                        "Stop"});   // ru
        tagsTable.put( "start_new_game", new String[]         // Tag Id
                                      { "",     // ct 
                                        "",         // de
                                        "",     // en
                                        "",     // es
                                        ""});   // ru
        tagsTable.put( "cancel_current_game", new String[]         // Tag Id
                                      { "",     // ct 
                                        "",         // de
                                        "",     // en
                                        "",     // es
                                        ""});   // ru
        tagsTable.put( "difficulty_level", new String[]         // Tag Id
                                      { "Nivell de dificultat",     // ct 
                                        "",         // de
                                        "Difficulty level",     // en
                                        "Nivel de dificultad",     // es
                                        ""});   // ru
        tagsTable.put( "", new String[]         // Tag Id
                                      { "",     // ct 
                                        "",         // de
                                        "",     // en
                                        "",     // es
                                        ""});   // ru
        tagsTable.put( "click_start", new String[]         // Tag Id
                                      { "Premi START per començar!",     // ct 
                                        "",         // de
                                        "Click the START Button!",     // en
                                        "Pulse START para empezar!",     // es
                                        ""});   // ru
    }
    
    /**
     * @param string
     * @return
     */
    static private String getCyrilicISO(String s)
    {
        String res = null;
        try
        {
            res = new String( s.getBytes(), "ISO-8859-5");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("Wrong Cyrilic string: " + s);
        }
        return res;
    }

    public Trans( String lang)
    {
        setLang( lang);
        initTagsTable();
        transInst = this;
    }
    
    public void setLang( String lang)
    {
        currentLang = -1;
        for (int i = 0; i < langIds.length; i++)
        {
            if( langIds[i].equals( lang) )
            {
                currentLang = i;
                break;
            }
        }
        if( currentLang == -1)
            throw new RuntimeException("Unknow language code: " + lang);
    }
    
    public String tag( String tagId)
    {
        String[] res = (String[] )tagsTable.get( tagId);
        try
        {
            if( res == null)
            {
                CONSTS.log("Tag not found: [" + tagId + "]");
                return "[" + tagId + "]";
            } else if (res[currentLang].length() == 0)
            {
                CONSTS.log("Tag [" + tagId + "] not translated to '" + langIds[currentLang] + "'.");
                return "[" + tagId + "]";
            }
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("Error with tag [" + tagId + "] language id [" + currentLang + "]\n" +
                    " Exception: " + e);
        }
        return res[currentLang];        
    }
    
    public static Trans get()
    {
        if( transInst == null)
            throw new RuntimeException("Translations not intialized!");
        return transInst;
    }
    
}
