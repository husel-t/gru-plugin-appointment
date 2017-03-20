package fr.paris.lutece.plugins.appointment.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import fr.paris.lutece.plugins.appointment.business.AppointmentForm;
import fr.paris.lutece.plugins.appointment.business.rule.ReservationRule;
import fr.paris.lutece.plugins.appointment.business.rule.ReservationRuleHome;
import fr.paris.lutece.util.ReferenceList;

public class ReservationRuleService {

	/**
	 * 
	 * @param appointmentForm
	 * @param nIdForm
	 * @param dateOfApply
	 * @return
	 */
	public static ReservationRule createReservationRule(AppointmentForm appointmentForm, int nIdForm,
			LocalDate dateOfApply) {
		ReservationRule reservationRule = new ReservationRule();
		fillInReservationRule(reservationRule, appointmentForm, nIdForm, dateOfApply);
		ReservationRuleHome.create(reservationRule);
		return reservationRule;
	}

	/**
	 * 
	 * @param appointmentForm
	 * @param nIdForm
	 * @param dateOfApply
	 * @return
	 */
	public static ReservationRule updateReservationRule(AppointmentForm appointmentForm, int nIdForm,
			LocalDate dateOfApply) {
		ReservationRule reservationRule = ReservationRuleService.findReservationRuleByIdFormAndDateOfApply(nIdForm,
				dateOfApply);
		if (reservationRule == null) {
			reservationRule = createReservationRule(appointmentForm, nIdForm, dateOfApply);
		} else {
			fillInReservationRule(reservationRule, appointmentForm, nIdForm, dateOfApply);
			ReservationRuleHome.update(reservationRule);
		}
		return reservationRule;
	}

	/**
	 * 
	 * @param reservationRule
	 * @param appointmentForm
	 * @param nIdForm
	 * @param dateOfApply
	 */
	public static void fillInReservationRule(ReservationRule reservationRule, AppointmentForm appointmentForm,
			int nIdForm, LocalDate dateOfApply) {
		reservationRule.setDateOfApply(dateOfApply);
		reservationRule.setMaxCapacityPerSlot(appointmentForm.getMaxCapacityPerSlot());
		reservationRule.setMaxPeoplePerAppointment(appointmentForm.getMaxPeoplePerAppointment());
		reservationRule.setIdForm(nIdForm);
	}

	/**
	 * 
	 * @param nIdForm
	 * @param dateOfApply
	 * @return
	 */
	public static ReservationRule findReservationRuleByIdFormAndClosestToDateOfApply(int nIdForm,
			LocalDate dateOfApply) {
		ReservationRule reservationRule = ReservationRuleHome.findByIdFormAndClosestToDateOfApply(nIdForm, dateOfApply);
		return reservationRule;
	}

	/**
	 * 
	 * @param nIdForm
	 * @param dateOfApply
	 * @return
	 */
	public static ReservationRule findReservationRuleByIdFormAndDateOfApply(int nIdForm, LocalDate dateOfApply) {
		ReservationRule reservationRule = ReservationRuleHome.findByIdFormAndDateOfApply(nIdForm, dateOfApply);
		return reservationRule;
	}

	/**
	 * 
	 * @param nIdReservationRule
	 * @return
	 */
	public static ReservationRule findReservationRuleById(int nIdReservationRule) {
		ReservationRule reservationRule = ReservationRuleHome.findByPrimaryKey(nIdReservationRule);
		return reservationRule;
	}

	/**
	 * 
	 * @param nIdForm
	 * @return
	 */
	public static ReferenceList findAllDateOfReservationRule(int nIdForm) {
		ReferenceList listDate = new ReferenceList();
		List<ReservationRule> listReservationRule = ReservationRuleHome.findByIdForm(nIdForm);
		for (ReservationRule reservationRule : listReservationRule) {
			listDate.addItem(reservationRule.getIdReservationRule(),
					reservationRule.getDateOfApply().format(Utilities.formatter));
		}
		return listDate;
	}

	public static HashMap<LocalDate, ReservationRule> findAllReservationRule(int nIdForm) {
		HashMap<LocalDate, ReservationRule> mapReservationRule = new HashMap<>();
		List<ReservationRule> listReservationRule = ReservationRuleHome.findByIdForm(nIdForm);
		for (ReservationRule reservationRule : listReservationRule) {
			mapReservationRule.put(reservationRule.getDateOfApply(), reservationRule);
		}
		return mapReservationRule;
	}

}
