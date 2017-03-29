/*
 * Copyright (c) 2002-2015, Mairie de Paris
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
package fr.paris.lutece.plugins.appointment.web;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.Strings;

import fr.paris.lutece.plugins.appointment.business.AppointmentForm;
import fr.paris.lutece.plugins.appointment.business.AppointmentFrontDTO;
import fr.paris.lutece.plugins.appointment.business.ResponseRecapDTO;
import fr.paris.lutece.plugins.appointment.business.appointment.Appointment;
import fr.paris.lutece.plugins.appointment.business.calendar.CalendarTemplate;
import fr.paris.lutece.plugins.appointment.business.calendar.CalendarTemplateHome;
import fr.paris.lutece.plugins.appointment.business.display.Display;
import fr.paris.lutece.plugins.appointment.business.form.Form;
import fr.paris.lutece.plugins.appointment.business.message.FormMessage;
import fr.paris.lutece.plugins.appointment.business.message.FormMessageHome;
import fr.paris.lutece.plugins.appointment.business.planning.WeekDefinition;
import fr.paris.lutece.plugins.appointment.business.rule.FormRule;
import fr.paris.lutece.plugins.appointment.business.rule.ReservationRule;
import fr.paris.lutece.plugins.appointment.business.slot.Slot;
import fr.paris.lutece.plugins.appointment.business.user.User;
import fr.paris.lutece.plugins.appointment.service.AppointmentPlugin;
import fr.paris.lutece.plugins.appointment.service.AppointmentService;
import fr.paris.lutece.plugins.appointment.service.DisplayService;
import fr.paris.lutece.plugins.appointment.service.EntryService;
import fr.paris.lutece.plugins.appointment.service.FormMessageService;
import fr.paris.lutece.plugins.appointment.service.FormRuleService;
import fr.paris.lutece.plugins.appointment.service.FormService;
import fr.paris.lutece.plugins.appointment.service.ReservationRuleService;
import fr.paris.lutece.plugins.appointment.service.SlotService;
import fr.paris.lutece.plugins.appointment.service.UserService;
import fr.paris.lutece.plugins.appointment.service.Utilities;
import fr.paris.lutece.plugins.appointment.service.WeekDefinitionService;
import fr.paris.lutece.plugins.appointment.service.upload.AppointmentAsynchronousUploadHandler;
import fr.paris.lutece.plugins.genericattributes.business.Entry;
import fr.paris.lutece.plugins.genericattributes.business.EntryHome;
import fr.paris.lutece.plugins.genericattributes.business.GenericAttributeError;
import fr.paris.lutece.plugins.genericattributes.business.Response;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.EntryTypeServiceManager;
import fr.paris.lutece.plugins.genericattributes.service.entrytype.IEntryTypeService;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.captcha.CaptchaSecurityService;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.image.ImageResource;
import fr.paris.lutece.portal.service.message.SiteMessage;
import fr.paris.lutece.portal.service.message.SiteMessageException;
import fr.paris.lutece.portal.service.message.SiteMessageService;
import fr.paris.lutece.portal.service.plugin.Plugin;
import fr.paris.lutece.portal.service.plugin.PluginService;
import fr.paris.lutece.portal.service.security.LuteceUser;
import fr.paris.lutece.portal.service.security.SecurityService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppException;
import fr.paris.lutece.portal.service.workflow.WorkflowService;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import fr.paris.lutece.portal.util.mvc.xpage.MVCApplication;
import fr.paris.lutece.portal.util.mvc.xpage.annotations.Controller;
import fr.paris.lutece.portal.web.xpages.XPage;
import fr.paris.lutece.util.beanvalidation.BeanValidationUtil;
import fr.paris.lutece.util.html.HtmlTemplate;
import fr.paris.lutece.util.sql.TransactionManager;

/**
 * This class provides a simple implementation of an Appointment XPage (On Front
 * Office)
 * 
 * @author Laurent Payen
 *
 */
@Controller(xpageName = AppointmentApp.XPAGE_NAME, pageTitleI18nKey = AppointmentApp.MESSAGE_DEFAULT_PAGE_TITLE, pagePathI18nKey = AppointmentApp.MESSAGE_DEFAULT_PATH)
public class AppointmentApp extends MVCApplication {

	/**
	 * Default page of XPages of this app
	 */
	public static final String MESSAGE_DEFAULT_PATH = "appointment.appointmentApp.defaultPath";

	/**
	 * Default page title of XPages of this app
	 */
	public static final String MESSAGE_DEFAULT_PAGE_TITLE = "appointment.appointmentApp.defaultTitle";

	/**
	 * The name of the XPage
	 */
	protected static final String XPAGE_NAME = "appointment";

	/**
	 * Generated serial version UID
	 */
	private static final long serialVersionUID = 5741361182728887387L;

	// Templates
	private static final String TEMPLATE_APPOINTMENT_FORM_LIST = "/skin/plugins/appointment/appointment_form_list.html";
	private static final String TEMPLATE_APPOINTMENT_FORM = "/skin/plugins/appointment/appointment_form.html";
	private static final String TEMPLATE_APPOINTMENT_FORM_RECAP = "/skin/plugins/appointment/appointment_form_recap.html";
	private static final String TEMPLATE_APPOINTMENT_CREATED = "skin/plugins/appointment/appointment_created.html";
	private static final String TEMPLATE_CANCEL_APPOINTMENT = "skin/plugins/appointment/cancel_appointment.html";
	private static final String TEMPLATE_APPOINTMENT_CANCELED = "skin/plugins/appointment/appointment_canceled.html";
	private static final String TEMPLATE_MY_APPOINTMENTS = "skin/plugins/appointment/my_appointments.html";
	private static final String TEMPLATE_HTML_CODE_FORM = "skin/plugins/appointment/html_code_form.html";

	// Views
	public static final String VIEW_APPOINTMENT_FORM = "getViewAppointmentForm";
	public static final String VIEW_APPOINTMENT_CALENDAR = "getViewAppointmentCalendar";
	private static final String VIEW_APPOINTMENT_FORM_LIST = "getViewFormList";
	private static final String VIEW_DISPLAY_RECAP_APPOINTMENT = "displayRecapAppointment";
	private static final String VIEW_GET_APPOINTMENT_CREATED = "getAppointmentCreated";
	private static final String VIEW_GET_CANCEL_APPOINTMENT = "getCancelAppointment";
	private static final String VIEW_APPOINTMENT_CANCELED = "getAppointmentCanceled";
	private static final String VIEW_GET_MY_APPOINTMENTS = "getMyAppointments";
	private static final String VIEW_GET_VIEW_CANCEL_APPOINTMENT = "getViewCancelAppointment";

	// Actions
	private static final String ACTION_DO_VALIDATE_FORM = "doValidateForm";
	private static final String ACTION_DO_MAKE_APPOINTMENT = "doMakeAppointment";
	private static final String ACTION_DO_CANCEL_APPOINTMENT = "doCancelAppointment";

	// Parameters
	private static final String PARAMETER_STARTING_DATE_TIME = "starting_date_time";
	private static final String PARAMETER_ENDING_DATE_TIME = "ending_date_time";
	private static final String PARAMETER_IS_OPEN = "is_open";
	private static final String PARAMETER_MAX_CAPACITY = "max_capacity";
	private static final String PARAMETER_NB_WEEKS_TO_DISPLAY = "nb_weeks_to_display";
	private static final String PARAMETER_DATE_OF_DISPLAY = "date_of_display";
	private static final String PARAMETER_DAY_OF_WEEK = "dow";
	private static final String PARAMETER_ID_FORM = "id_form";
	private static final String PARAMETER_EVENTS = "events";
	private static final String PARAMETER_MIN_DURATION = "min_duration";
	private static final String PARAMETER_MIN_TIME = "min_time";
	private static final String PARAMETER_MAX_TIME = "max_time";
	private static final String PARAMETER_EMAIL = "email";
	private static final String PARAMETER_EMAIL_CONFIRMATION = "emailConfirm";
	private static final String PARAMETER_FIRST_NAME = "firstname";
	private static final String PARAMETER_LAST_NAME = "lastname";
	private static final String PARAMETER_NUMBER_OF_BOOKED_SEATS = "nbBookedSeats";
	private static final String PARAMETER_ID_SLOT = "id_slot";
	private static final String PARAMETER_ID_APPOINTMENT = "id_appointment";
	private static final String PARAMETER_BACK = "back";
	private static final String PARAMETER_REF_APPOINTMENT = "refAppointment";
	private static final String PARAMETER_DATE_APPOINTMENT = "dateAppointment";
	private static final String PARAMETER_FROM_MY_APPOINTMENTS = "fromMyappointments";
	private static final String PARAMETER_REFERER = "referer";

	// Mark
	private static final String MARK_LOCALE = "locale";
	private static final String MARK_FORM = "form";
	private static final String MARK_FORM_MESSAGES = "formMessages";
	private static final String MARK_STR_ENTRY = "str_entry";
	private static final String MARK_APPOINTMENT = "appointment";
	private static final String MARK_LIST_ERRORS = "listAllErrors";
	private static final String MARK_PLACES = "nbplaces";
	private static final String MARK_TIME_APPOINTMENT = "timeAppointment";
	private static final String MARK_FORM_LIST = "form_list";
	private static final String MARK_FORM_HTML = "form_html";
	private static final String MARK_FORM_ERRORS = "form_errors";
	private static final String MARK_FORM_CALENDAR_ERRORS = "formCalendarErrors";
	private static final String MARK_CAPTCHA = "captcha";
	private static final String MARK_REF = "%%REF%%";
	private static final String MARK_DATE_APP = "%%DATE%%";
	private static final String MARK_TIME_BEGIN = "%%HEURE_DEBUT%%";
	private static final String MARK_TIME_END = "%%HEURE_FIN%%";
	private static final String MARK_LIST_APPOINTMENTS = "list_appointments";
	private static final String MARK_BACK_URL = "backUrl";
	private static final String MARK_STATUS_RESERVED = "status_reserved";
	private static final String MARK_STATUS_UNRESERVED = "status_unreserved";
	private static final String MARK_FROM_URL = "fromUrl";
	private static final String MARK_LIST_RESPONSE_RECAP_DTO = "listResponseRecapDTO";
	private static final String MARK_TITLE = "title";
	private static final String MARK_DATA = "data";
	private static final String MARK_BASE_64 = "base64";
	private static final String MARK_SEMI_COLON = ";";
	private static final String MARK_COMMA = ",";
	private static final String MARK_COLON = ":";
	private static final String MARK_ICONS = "icons";
	private static final String MARK_ICON_NULL = "NULL";

	// Errors
	private static final String ERROR_MESSAGE_SLOT_FULL = "appointment.message.error.slotFull";
	private static final String ERROR_MESSAGE_CAPTCHA = "portal.admin.message.wrongCaptcha";
	private static final String ERROR_MESSAGE_UNKNOWN_REF = "appointment.message.error.unknownRef";
	private static final String ERROR_MESSAGE_CAN_NOT_CANCEL_APPOINTMENT = "appointment.message.error.canNotCancelAppointment";
	private static final String ERROR_MESSAGE_EMPTY_CONFIRM_EMAIL = "appointment.validation.appointment.EmailConfirmation.email";
	private static final String ERROR_MESSAGE_CONFIRM_EMAIL = "appointment.message.error.confirmEmail";
	private static final String ERROR_MESSAGE_EMPTY_EMAIL = "appointment.validation.appointment.Email.notEmpty";
	private static final String ERROR_MESSAGE_EMPTY_NB_BOOKED_SEAT = "appointment.validation.appointment.NbBookedSeat.notEmpty";
	private static final String ERROR_MESSAGE_ERROR_NB_BOOKED_SEAT = "appointment.validation.appointment.NbBookedSeat.error";
	private static final String ERROR_MESSAGE_NB_MIN_DAYS_BETWEEN_TWO_APPOINTMENTS = "appointment.validation.appointment.NbMinDaysBetweenTwoAppointments.error";
	private static final String ERROR_MESSAGE_FORM_NOT_ACTIVE = "appointment.validation.appointment.formNotActive";
	private static final String ERROR_MESSAGE_NO_STARTING_VALIDITY_DATE = "appointment.validation.appointment.noStartingValidityDate";
	private static final String ERROR_MESSAGE_FORM_NO_MORE_VALID = "appointment.validation.appointment.formNoMoreValid";
	private static final String ERROR_MESSAGE_NO_AVAILABLE_SLOT = "appointment.validation.appointment.noAvailableSlot";

	// Session keys
	private static final String SESSION_APPOINTMENT_FORM_ERRORS = "appointment.session.formErrors";
	private static final String SESSION_NOT_VALIDATED_APPOINTMENT = "appointment.appointmentFormService.notValidatedAppointment";
	private static final String SESSION_VALIDATED_APPOINTMENT = "appointment.appointmentFormService.validatedAppointment";
	private static final String SESSION_ATTRIBUTE_APPOINTMENT_FORM = "appointment.session.appointmentForm";

	// Messages
	private static final String MESSAGE_CANCEL_APPOINTMENT_PAGE_TITLE = "appointment.cancel_appointment.pageTitle";
	private static final String MESSAGE_MY_APPOINTMENTS_PAGE_TITLE = "appointment.my_appointments.pageTitle";

	// Local variables
	private transient CaptchaSecurityService _captchaSecurityService;

	/**
	 * Get the calendar view
	 * 
	 * @param request
	 * @return the Xpage
	 */
	@View(VIEW_APPOINTMENT_CALENDAR)
	public XPage getViewAppointmentCalendar(HttpServletRequest request) {
		clearSession(request);
		int nIdForm = Integer.parseInt(request.getParameter(PARAMETER_ID_FORM));
		Map<String, Object> model = getModel();
		Form form = FormService.findFormLightByPrimaryKey(nIdForm);
		boolean bError = false;
		if (!form.isActive()) {
			addError(ERROR_MESSAGE_FORM_NOT_ACTIVE, getLocale(request));
			bError = true;
		}
		FormMessage formMessages = FormMessageHome.findByPrimaryKey(nIdForm);
		// Check if the date of display and the endDateOfDisplay are in the
		// validity date range of the form
		LocalDate startingValidityDate = form.getStartingValidityDate();
		if (startingValidityDate == null) {
			addError(ERROR_MESSAGE_NO_STARTING_VALIDITY_DATE, getLocale(request));
			bError = true;
		}
		LocalDate startingDateOfDisplay = LocalDate.now();
		if (startingValidityDate != null && startingValidityDate.isAfter(startingDateOfDisplay)) {
			startingDateOfDisplay = startingValidityDate;
		}
		Display display = DisplayService.findDisplayWithFormId(nIdForm);
		// Get the nb weeks to display
		int nNbWeeksToDisplay = display.getNbWeeksToDisplay();
		// Calculate the ending date of display with the nb weeks to display
		// since today
		LocalDate endingDateOfDisplay = startingDateOfDisplay.plus(nNbWeeksToDisplay, ChronoUnit.WEEKS);
		// if the ending date of display is after the ending validity date of
		// the form
		// assign the ending date of display with the ending validity date of
		// the form
		LocalDate endingValidityDate = form.getEndingValidityDate();
		if (endingValidityDate != null) {
			if (endingDateOfDisplay.isAfter(endingValidityDate)) {
				endingDateOfDisplay = endingValidityDate;
			}
			if (startingDateOfDisplay.isAfter(endingDateOfDisplay)) {
				addError(ERROR_MESSAGE_FORM_NO_MORE_VALID, getLocale(request));
				bError = true;
			}
		}
		LocalDate firstDateOfFreeOpenSlot = SlotService.findFirstDateOfFreeOpenSlot(nIdForm, startingDateOfDisplay,
				endingDateOfDisplay);
		if (firstDateOfFreeOpenSlot == null) {
			addError(ERROR_MESSAGE_NO_AVAILABLE_SLOT, getLocale(request));
			bError = true;
		}
		// Get the current date of display of the calendar, if it exists
		String strDateOfDisplay = request.getParameter(PARAMETER_DATE_OF_DISPLAY);
		LocalDate dateOfDisplay = startingDateOfDisplay;
		if (StringUtils.isNotEmpty(strDateOfDisplay)) {
			dateOfDisplay = LocalDate.parse(strDateOfDisplay);
		}
		// Get all the week definitions
		HashMap<LocalDate, WeekDefinition> mapWeekDefinition = WeekDefinitionService.findAllWeekDefinition(nIdForm);
		List<WeekDefinition> listWeekDefinition = new ArrayList<WeekDefinition>(mapWeekDefinition.values());
		// Get the min time of all the week definitions
		LocalTime minStartingTime = WeekDefinitionService.getMinStartingTimeOfAListOfWeekDefinition(listWeekDefinition);
		// Get the max time of all the week definitions
		LocalTime maxEndingTime = WeekDefinitionService.getMaxEndingTimeOfAListOfWeekDefinition(listWeekDefinition);
		// Get the min duration of an appointment of all the week definitions
		int nMinDuration = WeekDefinitionService.getMinDurationTimeSlotOfAListOfWeekDefinition(listWeekDefinition);
		// Get all the working days of all the week definitions
		List<String> listDayOfWeek = new ArrayList<>(
				WeekDefinitionService.getSetDayOfWeekOfAListOfWeekDefinition(listWeekDefinition));
		// Build the slots if no errors
		List<Slot> listSlot = new ArrayList<>();
		if (!bError) {
			listSlot = SlotService.buildListSlot(nIdForm, mapWeekDefinition, startingDateOfDisplay, nNbWeeksToDisplay);
			// Get the min time from now before a user can take an appointment
			// (in hours)
			FormRule formRule = FormRuleService.findFormRuleWithFormId(nIdForm);
			int minTimeBeforeAppointment = formRule.getMinTimeBeforeAppointment();
			LocalDateTime dateTimeBeforeAppointment = LocalDateTime.now().plus(minTimeBeforeAppointment,
					ChronoUnit.HOURS);
			// Filter the list of slots
			listSlot = listSlot.stream().filter(s -> s.getStartingDateTime().isAfter(dateTimeBeforeAppointment))
					.collect(Collectors.toList());
		}
		if (bError) {
			model.put(MARK_FORM_CALENDAR_ERRORS, bError);
		}
		model.put(PARAMETER_ID_FORM, nIdForm);
		model.put(MARK_FORM_MESSAGES, formMessages);
		model.put(PARAMETER_NB_WEEKS_TO_DISPLAY,
				Math.toIntExact(startingDateOfDisplay.until(endingDateOfDisplay, ChronoUnit.WEEKS)));
		model.put(PARAMETER_DATE_OF_DISPLAY, dateOfDisplay);
		model.put(PARAMETER_DAY_OF_WEEK, listDayOfWeek);
		model.put(PARAMETER_EVENTS, listSlot);
		model.put(PARAMETER_MIN_TIME, minStartingTime);
		model.put(PARAMETER_MAX_TIME, maxEndingTime);
		model.put(PARAMETER_MIN_DURATION, LocalTime.MIN.plusMinutes(nMinDuration));

		Locale locale = getLocale(request);
		CalendarTemplate calendarTemplate = CalendarTemplateHome.findByPrimaryKey(display.getIdCalendarTemplate());
		HtmlTemplate template = AppTemplateService.getTemplate(calendarTemplate.getTemplatePath(), locale, model);
		XPage xpage = new XPage();
		xpage.setContent(template.getHtml());
		xpage.setPathLabel(

				getDefaultPagePath(locale));
		xpage.setTitle(getDefaultPageTitle(locale));
		return xpage;
	}

	/**
	 * Get the form appointment view (front office)
	 * 
	 * @param request
	 *            the request
	 * @return the xpage
	 */
	@View(VIEW_APPOINTMENT_FORM)
	public XPage getViewAppointmentForm(HttpServletRequest request) {
		AppointmentForm form = (AppointmentForm) request.getSession().getAttribute(SESSION_ATTRIBUTE_APPOINTMENT_FORM);
		String strIdForm = request.getParameter(PARAMETER_ID_FORM);
		int nIdForm = Integer.parseInt(strIdForm);
		// Get the not validated appointment in session if it exists
		AppointmentFrontDTO appointmentFrontDTO = (AppointmentFrontDTO) request.getSession()
				.getAttribute(SESSION_NOT_VALIDATED_APPOINTMENT);
		if (appointmentFrontDTO == null) {
			// Try to get the validated appointment in session
			// (in case the user click on back button in the recap view
			appointmentFrontDTO = (AppointmentFrontDTO) request.getSession()
					.getAttribute(SESSION_VALIDATED_APPOINTMENT);
			if (appointmentFrontDTO == null) {
				appointmentFrontDTO = new AppointmentFrontDTO();
				int nIdSlot = Integer.parseInt(request.getParameter(PARAMETER_ID_SLOT));
				Slot slot = null;
				// If nIdSlot == 0, the slot has not been created yet
				if (nIdSlot == 0) {
					// Need to get all the informations to create the slot
					LocalDateTime startingDateTime = LocalDateTime
							.parse(request.getParameter(PARAMETER_STARTING_DATE_TIME));
					LocalDateTime endingDateTime = LocalDateTime
							.parse(request.getParameter(PARAMETER_ENDING_DATE_TIME));
					boolean bIsOpen = Boolean.parseBoolean(request.getParameter(PARAMETER_IS_OPEN));
					int nMaxCapacity = Integer.parseInt(request.getParameter(PARAMETER_MAX_CAPACITY));
					slot = SlotService.buildSlot(nIdForm, startingDateTime, endingDateTime, nMaxCapacity, nMaxCapacity,
							bIsOpen);
				} else {
					slot = SlotService.findSlotById(nIdSlot);
					SlotService.addDateAndTimeToSlot(slot);
				}

				appointmentFrontDTO.setSlot(slot);
				appointmentFrontDTO.setIdForm(nIdForm);
				LuteceUser user = SecurityService.getInstance().getRegisteredUser(request);
				if (user != null) {
					Map<String, String> map = user.getUserInfos();
					appointmentFrontDTO.setEmail(map.get("user.business-info.online.email"));
					appointmentFrontDTO.setFirstName(map.get("user.name.given"));
					appointmentFrontDTO.setLastName(map.get("user.name.family"));
				}
				request.getSession().setAttribute(SESSION_NOT_VALIDATED_APPOINTMENT, appointmentFrontDTO);
				ReservationRule reservationRule = ReservationRuleService
						.findReservationRuleByIdFormAndClosestToDateOfApply(nIdForm, slot.getDate());
				WeekDefinition weekDefinition = WeekDefinitionService
						.findWeekDefinitionByIdFormAndClosestToDateOfApply(nIdForm, slot.getDate());
				form = FormService.buildAppointmentForm(nIdForm, reservationRule.getIdReservationRule(),
						weekDefinition.getIdWeekDefinition());
				request.getSession().setAttribute(SESSION_ATTRIBUTE_APPOINTMENT_FORM, form);
			}
		}
		Locale locale = getLocale(request);
		StringBuffer strBuffer = new StringBuffer();
		List<Entry> listEntryFirstLevel = EntryService.getFilter(form.getIdForm(), true);
		for (Entry entry : listEntryFirstLevel) {
			EntryService.getHtmlEntry(entry.getIdEntry(), strBuffer, locale, true, request);
		}
		FormMessage formMessages = FormMessageService.findFormMessageByIdForm(nIdForm);
		Map<String, Object> model = getModel();
		model.put(MARK_APPOINTMENT, appointmentFrontDTO);
		model.put(PARAMETER_DATE_OF_DISPLAY, appointmentFrontDTO.getSlot().getDate());
		model.put(MARK_FORM, form);
		model.put(MARK_FORM_MESSAGES, formMessages);
		model.put(MARK_STR_ENTRY, strBuffer.toString());
		model.put(MARK_LOCALE, locale);
		model.put(MARK_PLACES, form.getMaxPeoplePerAppointment());
		List<GenericAttributeError> listErrors = (List<GenericAttributeError>) request.getSession()
				.getAttribute(SESSION_APPOINTMENT_FORM_ERRORS);
		model.put(MARK_FORM_ERRORS, listErrors);
		model.put(MARK_LIST_ERRORS, AppointmentFrontDTO.getAllErrors(locale));
		HtmlTemplate templateForm = AppTemplateService.getTemplate(TEMPLATE_HTML_CODE_FORM, locale, model);
		model.put(MARK_FORM_HTML, templateForm.getHtml());
		if (listErrors != null) {
			model.put(MARK_FORM_ERRORS, listErrors);
			request.getSession().removeAttribute(SESSION_APPOINTMENT_FORM_ERRORS);
		}
		HtmlTemplate template = AppTemplateService.getTemplate(TEMPLATE_APPOINTMENT_FORM, getLocale(request), model);
		XPage page = new XPage();
		page.setContent(template.getHtml());
		page.setPathLabel(getDefaultPagePath(getLocale(request)));
		if (form.getDisplayTitleFo()) {
			page.setTitle(form.getTitle());
		}
		return page;
	}

	/**
	 * Do validate data entered by a user to fill a form
	 * 
	 * @param request
	 *            The request
	 * @return The next URL to redirect to
	 * @throws SiteMessageException
	 * @throws UserNotSignedException
	 */
	@Action(ACTION_DO_VALIDATE_FORM)
	public XPage doValidateForm(HttpServletRequest request) throws SiteMessageException, UserNotSignedException {
		String strIdForm = request.getParameter(PARAMETER_ID_FORM);
		AppointmentFrontDTO appointmentFrontDTO = (AppointmentFrontDTO) request.getSession()
				.getAttribute(SESSION_NOT_VALIDATED_APPOINTMENT);
		AppointmentForm form = (AppointmentForm) request.getSession().getAttribute(SESSION_ATTRIBUTE_APPOINTMENT_FORM);
		int nIdForm = Integer.parseInt(strIdForm);
		List<GenericAttributeError> listFormErrors = new ArrayList<GenericAttributeError>();
		Locale locale = request.getLocale();
		String strEmail = request.getParameter(PARAMETER_EMAIL);
		checkEmail(strEmail, request.getParameter(PARAMETER_EMAIL_CONFIRMATION), form, locale, listFormErrors);
		if (!checkUserAndAppointment(appointmentFrontDTO.getSlot().getStartingDateTime().toLocalDate(), strEmail, form,
				locale, listFormErrors)) {
			return redirect(request, VIEW_APPOINTMENT_FORM, PARAMETER_ID_FORM, nIdForm, PARAMETER_ID_SLOT,
					appointmentFrontDTO.getSlot().getIdSlot());
		}
		int nbBookedSeats = checkAndReturnNbBookedSeats(request.getParameter(PARAMETER_NUMBER_OF_BOOKED_SEATS), form,
				appointmentFrontDTO.getSlot().getNbRemainingPlaces(), locale, listFormErrors);
		fillAppointmentFrontDTO(appointmentFrontDTO, nbBookedSeats, strEmail,
				request.getParameter(PARAMETER_FIRST_NAME), request.getParameter(PARAMETER_LAST_NAME));
		validateFormAndEntries(appointmentFrontDTO, request, listFormErrors);
		if (CollectionUtils.isNotEmpty(listFormErrors)) {
			request.getSession().setAttribute(SESSION_APPOINTMENT_FORM_ERRORS, listFormErrors);
			return redirect(request, VIEW_APPOINTMENT_FORM, PARAMETER_ID_FORM, nIdForm, PARAMETER_ID_SLOT,
					appointmentFrontDTO.getSlot().getIdSlot());
		}
		request.getSession().removeAttribute(SESSION_NOT_VALIDATED_APPOINTMENT);
		request.getSession().setAttribute(SESSION_VALIDATED_APPOINTMENT, appointmentFrontDTO);
		return redirectView(request, VIEW_DISPLAY_RECAP_APPOINTMENT);

	}

	/**
	 * Display the recap before validating an appointment
	 * 
	 * @param request
	 *            The request
	 * @return The HTML content to display or the next URL to redirect to
	 * @throws UserNotSignedException
	 */
	@View(VIEW_DISPLAY_RECAP_APPOINTMENT)
	public XPage displayRecapAppointment(HttpServletRequest request) throws UserNotSignedException {
		AppointmentFrontDTO appointment = (AppointmentFrontDTO) request.getSession()
				.getAttribute(SESSION_VALIDATED_APPOINTMENT);
		if (appointment == null) {
			return redirectView(request, VIEW_APPOINTMENT_FORM_LIST);
		}
		AppointmentForm form = (AppointmentForm) request.getSession().getAttribute(SESSION_ATTRIBUTE_APPOINTMENT_FORM);
		Map<String, Object> model = new HashMap<String, Object>();
		if (form.getEnableCaptcha() && getCaptchaService().isAvailable()) {
			model.put(MARK_CAPTCHA, getCaptchaService().getHtmlCode());
		}
		model.put(MARK_FORM_MESSAGES, FormMessageService.findFormMessageByIdForm(appointment.getIdForm()));
		fillCommons(model);
		model.put(MARK_APPOINTMENT, appointment);
		Locale locale = getLocale(request);
		List<ResponseRecapDTO> listResponseRecapDTO = new ArrayList<ResponseRecapDTO>(
				appointment.getListResponse().size());
		for (Response response : appointment.getListResponse()) {
			IEntryTypeService entryTypeService = EntryTypeServiceManager.getEntryTypeService(response.getEntry());
			listResponseRecapDTO.add(new ResponseRecapDTO(response,
					entryTypeService.getResponseValueForRecap(response.getEntry(), request, response, locale)));
		}
		model.put(MARK_LIST_RESPONSE_RECAP_DTO, listResponseRecapDTO);
		model.put(MARK_FORM, form);
		return getXPage(TEMPLATE_APPOINTMENT_FORM_RECAP, getLocale(request), model);
	}

	/**
	 * Do save an appointment into the database if it is valid
	 * 
	 * @param request
	 *            The request
	 * @return The XPage to display
	 * @throws UserNotSignedException
	 */
	@Action(ACTION_DO_MAKE_APPOINTMENT)
	public XPage doMakeAppointment(HttpServletRequest request) throws UserNotSignedException {
		AppointmentFrontDTO appointment = (AppointmentFrontDTO) request.getSession()
				.getAttribute(SESSION_VALIDATED_APPOINTMENT);
		if (StringUtils.isNotEmpty(request.getParameter(PARAMETER_BACK))) {
			return redirect(request, VIEW_APPOINTMENT_FORM, PARAMETER_ID_FORM, appointment.getIdForm());
		}
		AppointmentForm form = (AppointmentForm) request.getSession().getAttribute(SESSION_ATTRIBUTE_APPOINTMENT_FORM);
		if (form.getEnableCaptcha() && getCaptchaService().isAvailable()) {
			if (!getCaptchaService().validate(request)) {
				addError(ERROR_MESSAGE_CAPTCHA, getLocale(request));
				return redirect(request, VIEW_DISPLAY_RECAP_APPOINTMENT, PARAMETER_ID_FORM, appointment.getIdForm());
			}
		}
		Slot slot = null;
		// Reload the slot from the database
		// The slot could have been taken since the beginning of the entry of
		// the form
		if (appointment.getSlot().getIdSlot() != 0) {
			slot = SlotService.findSlotById(appointment.getSlot().getIdSlot());
		} else {
			HashMap<LocalDateTime, Slot> mapSlot = SlotService.findSlotsByIdFormAndDateRange(appointment.getIdForm(),
					appointment.getSlot().getStartingDateTime(), appointment.getSlot().getEndingDateTime());
			if (!mapSlot.isEmpty()) {
				slot = mapSlot.get(appointment.getSlot().getStartingDateTime());
			} else {
				slot = appointment.getSlot();
			}
		}
		if (appointment.getNbBookedSeats() > slot.getNbRemainingPlaces()) {
			addError(ERROR_MESSAGE_SLOT_FULL, getLocale(request));
			return redirect(request, VIEW_APPOINTMENT_CALENDAR, PARAMETER_ID_FORM, appointment.getIdForm());
		}
		int nIdAppointment = AppointmentService.saveAppointment(appointment);
		request.getSession().removeAttribute(SESSION_VALIDATED_APPOINTMENT);
		AppointmentAsynchronousUploadHandler.getHandler().removeSessionFiles(request.getSession().getId());
		return redirect(request, VIEW_GET_APPOINTMENT_CREATED, PARAMETER_ID_FORM, appointment.getIdForm(),
				PARAMETER_ID_APPOINTMENT, nIdAppointment);
	}

	/**
	 * Get the page to notify the user that the appointment has been created
	 * 
	 * @param request
	 *            The request
	 * @return The XPage to display
	 */
	@View(VIEW_GET_APPOINTMENT_CREATED)
	public XPage getAppointmentCreated(HttpServletRequest request) {
		int nIdForm = Integer.parseInt(request.getParameter(PARAMETER_ID_FORM));
		int nIdAppointment = Integer.parseInt(request.getParameter(PARAMETER_ID_APPOINTMENT));
		Appointment appointment = AppointmentService.findAppointmentById(nIdAppointment);
		FormMessage formMessages = FormMessageHome.findByPrimaryKey(nIdForm);
		Slot slot = SlotService.findSlotById(appointment.getIdSlot());
		Form form = FormService.findFormLightByPrimaryKey(nIdForm);
		String strTimeBegin = slot.getStartingDateTime().toLocalTime().toString();
		String strTimeEnd = slot.getEndingDateTime().toLocalTime().toString();
		String strReference = StringUtils.EMPTY;
		if (!StringUtils.isEmpty(form.getReference())) {
			strReference = Strings.toUpperCase(form.getReference().trim()) + " - ";
		}
		strReference += appointment.getReference();
		formMessages
				.setTextAppointmentCreated(formMessages.getTextAppointmentCreated().replaceAll(MARK_REF, strReference)
						.replaceAll(MARK_DATE_APP, slot.getStartingDateTime().toLocalDate().format(Utilities.formatter))
						.replaceAll(MARK_TIME_BEGIN, strTimeBegin).replaceAll(MARK_TIME_END, strTimeEnd));
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MARK_FORM, form);
		model.put(MARK_FORM_MESSAGES, formMessages);
		return getXPage(TEMPLATE_APPOINTMENT_CREATED, getLocale(request), model);
	}

	/**
	 * Get the view of all the forms on front office side
	 * 
	 * @param request
	 *            the request
	 * @return the xpage
	 */
	@View(value = VIEW_APPOINTMENT_FORM_LIST, defaultView = true)
	public XPage getFormList(HttpServletRequest request) {
		Locale locale = getLocale(request);
		String strHtmlContent = getFormListHtml(request, locale);
		XPage xpage = new XPage();
		xpage.setContent(strHtmlContent);
		xpage.setPathLabel(getDefaultPagePath(locale));
		xpage.setTitle(getDefaultPageTitle(locale));
		return xpage;
	}

	// TODO
	@View(VIEW_GET_CANCEL_APPOINTMENT)
	public XPage getCancelAppointment(HttpServletRequest request) {

		Map<String, Object> model = new HashMap<String, Object>();

		// model.put( MARK_FORM, form );
		Locale locale = getLocale(request);
		XPage xpage = getXPage(TEMPLATE_CANCEL_APPOINTMENT, locale, model);
		xpage.setTitle(I18nService.getLocalizedString(MESSAGE_CANCEL_APPOINTMENT_PAGE_TITLE, locale));

		return xpage;
	}

	// TODO
	@View(VIEW_GET_VIEW_CANCEL_APPOINTMENT)
	public XPage getViewCancelAppointment(HttpServletRequest request) throws SiteMessageException {
		String refAppointment = request.getParameter(PARAMETER_REF_APPOINTMENT);
		String strIdAppointment = "";// refAppointment.substring(0,
		// refAppointment.length() -
		// OldAppointmentService.getInstance().getRefSizeRandomPart());
		Appointment appointment = null;

		if (StringUtils.isNotEmpty(strIdAppointment) && StringUtils.isNumeric(strIdAppointment)) {
			int nIdAppointment = Integer.parseInt(strIdAppointment);
			// appointment =
			// OldAppointmentHome.findByPrimaryKey(nIdAppointment);

		}
		Map<String, Object> model = new HashMap<String, Object>();

		model.put(PARAMETER_REF_APPOINTMENT, refAppointment);

		if (appointment != null) {
			// model.put(MARK_DATE_APPOINTMENT,
			// appointment.getDateAppointment());
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
			// String currentTime =
			// simpleDateFormat.format(appointment.getStartAppointment());
			model.put(MARK_TIME_APPOINTMENT, "");
		} else {
			SiteMessageService.setMessage(request, ERROR_MESSAGE_CAN_NOT_CANCEL_APPOINTMENT, SiteMessage.TYPE_STOP);
		}
		Locale locale = getLocale(request);
		XPage xpage = getXPage(TEMPLATE_CANCEL_APPOINTMENT, locale, model);
		xpage.setTitle(I18nService.getLocalizedString(MESSAGE_CANCEL_APPOINTMENT_PAGE_TITLE, locale));

		return xpage;
	}

	// TODO
	@Action(ACTION_DO_CANCEL_APPOINTMENT)
	public XPage doCancelAppointment(HttpServletRequest request) throws SiteMessageException {
		String strRef = request.getParameter(PARAMETER_REF_APPOINTMENT);

		String strIdAppointment = "";// strRef.substring(0,
		// strRef.length() -
		// OldAppointmentService.getInstance().getRefSizeRandomPart());
		String strDate = request.getParameter(PARAMETER_DATE_APPOINTMENT);

		if (StringUtils.isNotEmpty(strIdAppointment) && StringUtils.isNumeric(strIdAppointment)) {
			int nIdAppointment = Integer.parseInt(strIdAppointment);
			Appointment appointment = null;// OldAppointmentHome.findByPrimaryKey(nIdAppointment);

			// Date date = (Date) getDateConverter().convert(Date.class,
			// strDate);

			if (StringUtils.equals(strRef, "")) {// OldAppointmentService.getInstance().computeRefAppointment(appointment))
				// && DateUtils.isSameDay(date,
				// appointment.getDateAppointment())) {
				// AppointmentSlot appointmentSlot =
				// AppointmentSlotHome.findByPrimaryKey(appointment.getIdSlot());
				// AppointmentForm form =
				// AppointmentFormHome.findByPrimaryKey(appointmentSlot.getIdForm());

				Plugin appointmentPlugin = PluginService.getPlugin(AppointmentPlugin.PLUGIN_NAME);

				TransactionManager.beginTransaction(appointmentPlugin);

				try {
					if (true) {// appointment.getIdActionCancel() > 0) {
						boolean automaticUpdate = (AdminUserService.getAdminUser(request) == null) ? true : false;
						WorkflowService.getInstance().doProcessAction(appointment.getIdAppointment(), "",
								// Appointment.APPOINTMENT_RESOURCE_TYPE,
								1,
								// appointment.getIdActionCancel(),
								// form.getIdForm(),
								1, request, request.getLocale(), automaticUpdate);
					} else {
						// appointment.setStatus(Appointment.Status.STATUS_UNRESERVED.getValeur());
						// OldAppointmentHome.update(appointment);
					}

					TransactionManager.commitTransaction(appointmentPlugin);
				} catch (Exception e) {
					TransactionManager.rollBack(appointmentPlugin);
					throw new AppException(e.getMessage(), e);
				}

				if (StringUtils.isNotEmpty(strRef)) {
					Map<String, String> mapParameters = new HashMap<String, String>();

					if (StringUtils.isNotEmpty(request.getParameter(PARAMETER_FROM_MY_APPOINTMENTS))) {
						String strReferer = request.getHeader(PARAMETER_REFERER);

						if (StringUtils.isNotEmpty(strReferer)) {
							mapParameters.put(MARK_FROM_URL, strReferer);
						}

						mapParameters.put(PARAMETER_FROM_MY_APPOINTMENTS,
								request.getParameter(PARAMETER_FROM_MY_APPOINTMENTS));
					}

					// mapParameters.put(PARAMETER_ID_FORM,
					// Integer.toString(appointmentSlot.getIdForm()));

					return redirect(request, VIEW_APPOINTMENT_CANCELED, mapParameters);
				}

				SiteMessageService.setMessage(request, ERROR_MESSAGE_UNKNOWN_REF, SiteMessage.TYPE_STOP);
			}
		}

		return redirectView(request, VIEW_APPOINTMENT_FORM_LIST);
	}

	// TODO
	@View(VIEW_APPOINTMENT_CANCELED)
	public XPage getAppointmentCanceled(HttpServletRequest request) {
		String strIdForm = request.getParameter(PARAMETER_ID_FORM);

		if (StringUtils.isNotEmpty(strIdForm) && StringUtils.isNumeric(strIdForm)) {
			int nIdForm = Integer.parseInt(strIdForm);

			Map<String, Object> model = new HashMap<String, Object>();
			model.put(MARK_FORM_MESSAGES, FormMessageHome.findByPrimaryKey(nIdForm));

			if (Boolean.parseBoolean(request.getParameter(PARAMETER_FROM_MY_APPOINTMENTS))) {
				String strFromUrl = request.getParameter(MARK_FROM_URL);
				model.put(MARK_BACK_URL,
						StringUtils.isNotEmpty(strFromUrl) ? strFromUrl : getViewUrl(VIEW_GET_MY_APPOINTMENTS));
			}

			return getXPage(TEMPLATE_APPOINTMENT_CANCELED, getLocale(request), model);
		}

		return redirectView(request, VIEW_APPOINTMENT_FORM_LIST);
	}

	// TODO
	@View(VIEW_GET_MY_APPOINTMENTS)
	public XPage getMyAppointments(HttpServletRequest request) throws UserNotSignedException {
		if (!SecurityService.isAuthenticationEnable()) {
			return redirectView(request, VIEW_APPOINTMENT_FORM_LIST);
		}

		XPage xpage = new XPage();
		Locale locale = getLocale(request);
		xpage.setContent(getMyAppointmentsXPage(request, locale));
		xpage.setTitle(I18nService.getLocalizedString(MESSAGE_MY_APPOINTMENTS_PAGE_TITLE, locale));

		return xpage;
	}

	// TODO
	public static String getMyAppointmentsXPage(HttpServletRequest request, Locale locale)
			throws UserNotSignedException {
		if (!SecurityService.isAuthenticationEnable()) {
			return null;
		}

		LuteceUser luteceUser = SecurityService.getInstance().getRegisteredUser(request);

		if (luteceUser == null) {
			throw new UserNotSignedException();
		}

		// AppointmentFilter appointmentFilter = new AppointmentFilter();
		// appointmentFilter.setIdUser(luteceUser.getName());
		// appointmentFilter.setAuthenticationService(luteceUser.getAuthenticationService());
		// appointmentFilter.setDateAppointmentMin(new
		// Date(System.currentTimeMillis()));

		List<Appointment> listAppointments = null;// OldAppointmentHome.getAppointmentListByFilter(appointmentFilter);

		// List<AppointmentDTO> listAppointmentDTO = new
		// ArrayList<AppointmentDTO>(listAppointments.size());
		//
		// Map<String, String> lsSta = new HashMap<String, String>();
		// for (Appointment appointment : listAppointments) {
		// AppointmentDTO appointmentDTO = null;// new
		// // AppointmentDTO(appointment);
		// appointmentDTO.setAppointmentSlot(AppointmentSlotHome.findByPrimaryKey(appointment.getIdSlot()));
		// /*
		// * WORKFLOW FUTURE if (!nidForm.contains( Integer.valueOf(
		// * appointmantDTO.getAppointmentSlot( ).getIdForm( ) ) )); {
		// * nidForm.add(Integer.valueOf( appointmantDTO.getAppointmentSlot(
		// * ).getIdForm( ) ) ); }
		// */
		// appointmentDTO.setAppointmentForm(
		// AppointmentFormHome.findByPrimaryKey(appointmentDTO.getAppointmentSlot().getIdForm()));
		// listAppointmentDTO.add(appointmentDTO);
		// }

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MARK_LIST_APPOINTMENTS, "");
		/*
		 * WORKFLOW FUTURE model.put( MARK_STATUS, getAllStatus (nidForm));
		 */
		// model.put(MARK_STATUS_RESERVED,
		// Appointment.Status.STATUS_RESERVED.getValeur());
		// model.put(MARK_STATUS_UNRESERVED,
		// Appointment.Status.STATUS_UNRESERVED.getValeur());

		HtmlTemplate template = AppTemplateService.getTemplate(TEMPLATE_MY_APPOINTMENTS, locale, model);

		return template.getHtml();
	}

	/**
	 * Get the html content of the list of forms
	 * 
	 * @param appointmentFormService
	 *            The service to use
	 * @param strTitle
	 *            The title to display, or null to display the default title.
	 * @param locale
	 *            The locale
	 * @return The HTML content to display
	 */
	public static String getFormListHtml(HttpServletRequest request, Locale locale) {
		request.getSession().removeAttribute(SESSION_VALIDATED_APPOINTMENT);
		Map<String, Object> model = new HashMap<String, Object>();
		// We keep only the active
		List<AppointmentForm> listAppointmentForm = FormService.buildAllActiveAppointmentForm().stream()
				.filter(a -> a.getDateStartValidity().toLocalDate().isBefore(LocalDate.now())
						|| a.getDateStartValidity().toLocalDate().equals(LocalDate.now()))
				.sorted((a1, a2) -> a1.getTitle().compareTo(a2.getTitle())).collect(Collectors.toList());
		List<String> icons = new ArrayList<String>();
		for (AppointmentForm form : listAppointmentForm) {
			ImageResource img = form.getIcon();
			if (img == null || img.getImage() == null || StringUtils.isEmpty(img.getMimeType())
					|| StringUtils.equals(img.getMimeType(), MARK_ICON_NULL)) {
				icons.add(MARK_ICON_NULL);
			} else {
				byte[] imgBytesAsBase64 = Base64.encodeBase64(img.getImage());
				String imgDataAsBase64 = new String(imgBytesAsBase64);
				String strMimeType = img.getMimeType();
				String imgAsBase64 = MARK_DATA + MARK_COLON + strMimeType + MARK_SEMI_COLON + MARK_BASE_64 + MARK_COMMA
						+ imgDataAsBase64;
				icons.add(imgAsBase64);
			}
		}
		model.put(MARK_ICONS, icons);
		model.put(MARK_FORM_LIST, listAppointmentForm);
		HtmlTemplate template = AppTemplateService.getTemplate(TEMPLATE_APPOINTMENT_FORM_LIST, locale, model);
		return template.getHtml();
	}

	/**
	 * Get the captcha security service
	 * 
	 * @return The captcha security service
	 */
	private CaptchaSecurityService getCaptchaService() {
		if (_captchaSecurityService == null) {
			_captchaSecurityService = new CaptchaSecurityService();
		}

		return _captchaSecurityService;
	}

	/**
	 * Check that the email is correct and matches the confirm email
	 * 
	 * @param strEmail
	 *            the email
	 * @param strConfirmEmail
	 *            the confirm email
	 * @param form
	 *            the form
	 * @param locale
	 *            the local
	 * @param listFormErrors
	 *            the list of errors that can be fill in with the errors found
	 *            for the email
	 */
	private void checkEmail(String strEmail, String strConfirmEmail, AppointmentForm form, Locale locale,
			List<GenericAttributeError> listFormErrors) {
		if (form.getEnableMandatoryEmail()) {
			if (StringUtils.isEmpty(strEmail)) {
				GenericAttributeError genAttError = new GenericAttributeError();
				genAttError.setErrorMessage(I18nService.getLocalizedString(ERROR_MESSAGE_EMPTY_EMAIL, locale));
				listFormErrors.add(genAttError);
			}
			if (StringUtils.isEmpty(strConfirmEmail)) {
				GenericAttributeError genAttError = new GenericAttributeError();
				genAttError.setErrorMessage(I18nService.getLocalizedString(ERROR_MESSAGE_EMPTY_CONFIRM_EMAIL, locale));
				listFormErrors.add(genAttError);
			}
		}
		if (!StringUtils.equals(strEmail, strConfirmEmail)) {
			GenericAttributeError genAttError = new GenericAttributeError();
			genAttError.setErrorMessage(I18nService.getLocalizedString(ERROR_MESSAGE_CONFIRM_EMAIL, locale));
			listFormErrors.add(genAttError);
		}
	}

	/**
	 * Check and validate all the rules for the number of booked seats asked
	 * 
	 * @param strNbBookedSeats
	 *            the number of booked seats
	 * @param form
	 *            the form
	 * @param nbRemainingPlaces
	 *            the number of remaining places on the slot asked
	 * @param locale
	 *            the locale
	 * @param listFormErrors
	 *            the list of errors that can be fill in with the errors found
	 *            for the number of booked seats
	 * @return
	 */
	private int checkAndReturnNbBookedSeats(String strNbBookedSeats, AppointmentForm form, int nbRemainingPlaces,
			Locale locale, List<GenericAttributeError> listFormErrors) {
		int nbBookedSeats = 1;
		if (StringUtils.isEmpty(strNbBookedSeats) && form.getMaxPeoplePerAppointment() > 1) {
			GenericAttributeError genAttError = new GenericAttributeError();
			genAttError.setErrorMessage(I18nService.getLocalizedString(ERROR_MESSAGE_EMPTY_NB_BOOKED_SEAT, locale));
			listFormErrors.add(genAttError);
		}
		if (StringUtils.isNotEmpty(strNbBookedSeats)) {
			nbBookedSeats = Integer.parseInt(strNbBookedSeats);
		}
		if (nbBookedSeats > nbRemainingPlaces) {
			GenericAttributeError genAttError = new GenericAttributeError();
			genAttError.setErrorMessage(I18nService.getLocalizedString(ERROR_MESSAGE_ERROR_NB_BOOKED_SEAT, locale));
			listFormErrors.add(genAttError);
		}
		if (nbBookedSeats == 0) {
			GenericAttributeError genAttError = new GenericAttributeError();
			genAttError.setErrorMessage(I18nService.getLocalizedString(ERROR_MESSAGE_EMPTY_NB_BOOKED_SEAT, locale));
			listFormErrors.add(genAttError);
		}
		return nbBookedSeats;
	}

	/**
	 * Check that the user has no previous appointment or that the previous
	 * appointment respect the delay
	 * 
	 * @param dateOfTheAppointment
	 *            date of the new appointment
	 * @param strEmail
	 *            the email of the user
	 * @param form
	 *            the form
	 * @param locale
	 *            the locale
	 * @param listFormErrors
	 *            the list of errors that can be fill in with the errors found
	 * @return
	 */
	private boolean checkUserAndAppointment(LocalDate dateOfTheAppointment, String strEmail, AppointmentForm form,
			Locale locale, List<GenericAttributeError> listFormErrors) {
		boolean bCheckPassed = true;
		if (StringUtils.isNotEmpty(strEmail)) {
			int nbDaysBetweenTwoAppointments = form.getNbDaysBeforeNewAppointment();
			if (nbDaysBetweenTwoAppointments != 0) {
				// Looking for existing user with this email
				User user = UserService.findUserByEmail(strEmail);
				if (user != null) {
					// looking for its appointment
					List<Appointment> listAppointment = AppointmentService
							.findListAppointmentByUserId(user.getIdUser());
					if (CollectionUtils.isNotEmpty(listAppointment)) {
						// I know we could have a join sql query, but I don't
						// want to join the appointment table with the slot
						// table, it's too big and not efficient
						List<Slot> listSlot = new ArrayList<>();
						for (Appointment appointment : listAppointment) {
							listSlot.add(SlotService.findSlotById(appointment.getIdSlot()));
						}
						// Get the last appointment date for this form
						LocalDate dateOfTheLastAppointment = listSlot.stream()
								.filter(s -> s.getIdForm() == form.getIdForm()).map(Slot::getStartingDateTime)
								.max(LocalDateTime::compareTo).get().toLocalDate();
						// Check the number of days between this appointment and
						// the last appointment the user has taken
						if ((dateOfTheLastAppointment.isBefore(dateOfTheAppointment)
								|| dateOfTheLastAppointment.equals(dateOfTheAppointment))
								&& dateOfTheLastAppointment.until(dateOfTheAppointment,
										ChronoUnit.DAYS) <= nbDaysBetweenTwoAppointments) {
							addError(ERROR_MESSAGE_NB_MIN_DAYS_BETWEEN_TWO_APPOINTMENTS, locale);
							bCheckPassed = false;
						}
					}
				}
			}
		}
		return bCheckPassed;
	}

	/**
	 * Fill the appoinmentFront DTO with the given parameters
	 * 
	 * @param appointmentFrontDTO
	 *            the appointmentFront DTO
	 * @param nbBookedSeats
	 *            the number of booked seats
	 * @param strEmail
	 *            the email of the user
	 * @param strFirstName
	 *            the first name of the user
	 * @param strLastName
	 *            the last name of the user
	 */
	private void fillAppointmentFrontDTO(AppointmentFrontDTO appointmentFrontDTO, int nbBookedSeats, String strEmail,
			String strFirstName, String strLastName) {
		appointmentFrontDTO
				.setDateOfTheAppointment(appointmentFrontDTO.getSlot().getDate().format(Utilities.formatter));
		appointmentFrontDTO.setNbBookedSeats(nbBookedSeats);
		appointmentFrontDTO.setEmail(strEmail);
		appointmentFrontDTO.setFirstName(strFirstName);
		appointmentFrontDTO.setLastName(strLastName);
		Map<Integer, List<Response>> mapResponses = appointmentFrontDTO.getMapResponsesByIdEntry();
		if (mapResponses != null) {
			List<Response> listResponse = new ArrayList<Response>();
			for (List<Response> listResponseByEntry : mapResponses.values()) {
				listResponse.addAll(listResponseByEntry);
			}
			appointmentFrontDTO.setMapResponsesByIdEntry(null);
			appointmentFrontDTO.setListResponse(listResponse);
		}
	}

	/**
	 * Validate the form and the additional entries of the form
	 * 
	 * @param appointmentFrontDTO
	 *            the appointmentFron DTo to validate
	 * @param request
	 *            the request
	 * @param listFormErrors
	 *            the list of errors that can be fill with the errors found at
	 *            the validation
	 */
	private void validateFormAndEntries(AppointmentFrontDTO appointmentFrontDTO, HttpServletRequest request,
			List<GenericAttributeError> listFormErrors) {
		Set<ConstraintViolation<AppointmentFrontDTO>> listErrors = BeanValidationUtil.validate(appointmentFrontDTO);
		if (CollectionUtils.isNotEmpty(listErrors)) {
			for (ConstraintViolation<AppointmentFrontDTO> constraintViolation : listErrors) {
				GenericAttributeError genAttError = new GenericAttributeError();
				genAttError.setErrorMessage(
						I18nService.getLocalizedString(constraintViolation.getMessageTemplate(), request.getLocale()));
				listFormErrors.add(genAttError);
			}
		}
		List<Entry> listEntryFirstLevel = EntryHome
				.getEntryList(EntryService.buildEntryFilter(appointmentFrontDTO.getIdForm()));
		for (Entry entry : listEntryFirstLevel) {
			listFormErrors.addAll(EntryService.getResponseEntry(request, entry.getIdEntry(), request.getLocale(),
					appointmentFrontDTO));
		}
	}

	/**
	 * Clear the user's session
	 * 
	 * @param request
	 *            the request
	 */
	private void clearSession(HttpServletRequest request) {
		request.getSession().removeAttribute(SESSION_APPOINTMENT_FORM_ERRORS);
		request.getSession().removeAttribute(SESSION_ATTRIBUTE_APPOINTMENT_FORM);
		request.getSession().removeAttribute(SESSION_NOT_VALIDATED_APPOINTMENT);
		request.getSession().removeAttribute(SESSION_VALIDATED_APPOINTMENT);
	}
}
