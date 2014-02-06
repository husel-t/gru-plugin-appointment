/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.appointment.business;

import fr.paris.lutece.portal.service.util.AppLogService;


/**
 * Class to describe messages of appointment form displayed in FO
 */
public class AppointmentFormMessages implements Cloneable
{
    private int _nIdForm;
    private String _strCalendarTitle;
    private String _strFieldFirstNameTitle;
    private String _strFieldFirstNameHelp;
    private String _strFieldLastNameTitle;
    private String _strFieldLastNameHelp;
    private String _strFieldEmailTitle;
    private String _strFieldEmailHelp;
    private String _strUrlRedirectAfterCreation;
    private String _strTextAppointmentCreated;
    private String _strTextAppointmentCanceled;
    private String _strLabelButtonRedirection;

    /**
     * Return the id of the associated form
     * @return The id of the associated form
     */
    public int getIdForm(  )
    {
        return _nIdForm;
    }

    /**
     * Set the id of the associated form
     * @param nIdForm The id of the associated form
     */
    public void setIdForm( int nIdForm )
    {
        _nIdForm = nIdForm;
    }

    /**
     * Get the title of the page of the calendar in FO
     * @return the title of the page of the calendar in FO
     */
    public String getCalendarTitle(  )
    {
        return _strCalendarTitle;
    }

    /**
     * Set the title of the page of the calendar in FO
     * @param strCalendarTitle The title of the page of the calendar in FO
     */
    public void setCalendarTitle( String strCalendarTitle )
    {
        this._strCalendarTitle = strCalendarTitle;
    }

    /**
     * Get the title of the first name field in the form page in FO
     * @return The title of the first name field in the form page in FO
     */
    public String getFieldFirstNameTitle(  )
    {
        return _strFieldFirstNameTitle;
    }

    /**
     * Set the title of the first name field in the form page in FO
     * @param strFieldFirstNameTitle The title of the first name field in the
     *            form page in FO
     */
    public void setFieldFirstNameTitle( String strFieldFirstNameTitle )
    {
        this._strFieldFirstNameTitle = strFieldFirstNameTitle;
    }

    /**
     * Get the help message of the first name field in the form page in FO
     * @return The help message of the first name field in the form page in FO
     */
    public String getFieldFirstNameHelp(  )
    {
        return _strFieldFirstNameHelp;
    }

    /**
     * Set the help message of the first name field in the form page in FO
     * @param strFieldFirstNameHelp The help message of the first name field in
     *            the form page in FO
     */
    public void setFieldFirstNameHelp( String strFieldFirstNameHelp )
    {
        this._strFieldFirstNameHelp = strFieldFirstNameHelp;
    }

    /**
     * Get the title of the last name field in the form page in FO
     * @return The title of the last name field in the form page in FO
     */
    public String getFieldLastNameTitle(  )
    {
        return _strFieldLastNameTitle;
    }

    /**
     * Set the title of the last name field in the form page in FO
     * @param strFieldLastNameTitle The title of the last name field in the form
     *            page in FO
     */
    public void setFieldLastNameTitle( String strFieldLastNameTitle )
    {
        this._strFieldLastNameTitle = strFieldLastNameTitle;
    }

    /**
     * Get the help message of the last name field in the form page in FO
     * @return The help message of the last name field in the form page in FO
     */
    public String getFieldLastNameHelp(  )
    {
        return _strFieldLastNameHelp;
    }

    /**
     * Get the help message of the last name field in the form page in FO
     * @param strFieldLastNameHelp The help message of the last name field in
     *            the form page in FO
     */
    public void setFieldLastNameHelp( String strFieldLastNameHelp )
    {
        this._strFieldLastNameHelp = strFieldLastNameHelp;
    }

    /**
     * Get the title of the email field in the form page in FO
     * @return The title of the email field in the form page in FO
     */
    public String getFieldEmailTitle(  )
    {
        return _strFieldEmailTitle;
    }

    /**
     * Set the title of the email field in the form page in FO
     * @param strFieldEmailTitle The title of the email field in the form page
     *            in FO
     */
    public void setFieldEmailTitle( String strFieldEmailTitle )
    {
        this._strFieldEmailTitle = strFieldEmailTitle;
    }

    /**
     * Get the help message of the email field in the form page in FO
     * @return The help message of the email field in the form page in FO
     */
    public String getFieldEmailHelp(  )
    {
        return _strFieldEmailHelp;
    }

    /**
     * Set the help message of the email field in the form page in FO
     * @param strFieldEmailHelp The help message of the email field in the form
     *            page in FO
     */
    public void setFieldEmailHelp( String strFieldEmailHelp )
    {
        this._strFieldEmailHelp = strFieldEmailHelp;
    }

    /**
     * Get the URL to redirect the user to after creation of an appointment
     * @return The URL to redirect the user to after creation of an
     *         appointment
     */
    public String getUrlRedirectAfterCreation(  )
    {
        return _strUrlRedirectAfterCreation;
    }

    /**
     * Set the URL to redirect the user to after creation of an appointment
     * @param strUrlRedirectAfterCreation The URL to redirect the user to
     *            after creation of an appointment
     */
    public void setUrlRedirectAfterCreation( String strUrlRedirectAfterCreation )
    {
        this._strUrlRedirectAfterCreation = strUrlRedirectAfterCreation;
    }

    /**
     * Get the text to display to the user after the creation of an
     * appointment and before he is redirected to a given URL
     * @return The text to display to the user after the creation of an
     *         appointment
     */
    public String getTextAppointmentCreated(  )
    {
        return _strTextAppointmentCreated;
    }

    /**
     * Set the text to display to the user after the creation of an
     * appointment
     * @param strTextAppointmentCreated The text to display to the user after
     *            the creation of an appointment
     */
    public void setTextAppointmentCreated( String strTextAppointmentCreated )
    {
        this._strTextAppointmentCreated = strTextAppointmentCreated;
    }

    /**
     * Get the text to display to the user after the canceling of an
     * appointment
     * @return The text to display to the user after the canceling of an
     *         appointment
     */
    public String getTextAppointmentCanceled(  )
    {
        return _strTextAppointmentCanceled;
    }

    /**
     * Set the text to display to the user after the canceling of an
     * appointment
     * @param strTextAppointmentCanceled The text to display to the user after
     *            the canceling of an appointment
     */
    public void setTextAppointmentCanceled( String strTextAppointmentCanceled )
    {
        this._strTextAppointmentCanceled = strTextAppointmentCanceled;
    }

    /**
     * Get the label of the button after the creation of an appointment
     * @return The label of the button after the creation of an appointment
     */
    public String getLabelButtonRedirection(  )
    {
        return _strLabelButtonRedirection;
    }

    /**
     * Set the label of the button after the creation of an appointment
     * @param strLabelButtonRedirection The label of the button after the
     *            creation of an appointment
     */
    public void setLabelButtonRedirection( String strLabelButtonRedirection )
    {
        this._strLabelButtonRedirection = strLabelButtonRedirection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone(  )
    {
        try
        {
            return super.clone(  );
        }
        catch ( CloneNotSupportedException e )
        {
            AppLogService.error( e.getMessage(  ), e );

            return null;
        }
    }
}
