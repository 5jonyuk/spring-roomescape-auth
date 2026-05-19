package roomescape.reservation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.dto.response.ReservationSaveResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.projection.ReservationDetailProjection;
import roomescape.schedule.ScheduleService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final ScheduleService scheduleService;

    public ReservationSaveResponse save(ReservationSaveRequest body, long memberId) {
        scheduleService.validateSchedule(body.date(), body.timeId(), body.themeId());
        long scheduleId = scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(body.date(), body.timeId(), body.themeId());
        validateReservationAlreadyExistsNot(scheduleId);
        Reservation reservation = reservationRepository.save(body.toDomain(memberId, scheduleId));

        return ReservationSaveResponse.from(reservation);
    }

    public List<ReservationDetailFindResponse> findAllDetails() {
        return ReservationDetailFindResponse.from(reservationRepository.findAllDetails());
    }

    public void deleteByIdAndMemberId(long reservationId, long memberId) {
        ReservationDetailProjection reservationDetail = reservationRepository.findDetailByIdAndMemberId(reservationId, memberId)
                .orElse(null);
        if (reservationDetail == null) {
            return;
        }
        validateNotPast(reservationDetail);
        reservationRepository.deleteByIdAndMemberId(reservationId, memberId);
    }

    public List<ReservationDetailFindResponse> findMyReservations(long memberId) {
        List<ReservationDetailProjection> reservationDetailProjection = reservationRepository.findAllReservationDetailsByMemberId(memberId);

        return ReservationDetailFindResponse.from(reservationDetailProjection);
    }

    public ReservationSaveResponse update(ReservationUpdateRequest body, long reservationId, long memberId) {
        ReservationDetailProjection oldReservation = getOldReservationDetailOrThrow(reservationId, memberId);
        validateNotPast(oldReservation);
        validateNotEmptyUpdateRequest(body);

        LocalDate newDate = Objects.requireNonNullElse(body.date(), oldReservation.date());
        long newTimeId = Objects.requireNonNullElse(body.timeId(), oldReservation.getTimeId());
        long scheduleId = scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(newDate, newTimeId, oldReservation.getThemeId());
        scheduleService.validateSchedule(newDate, newTimeId, oldReservation.getThemeId());
        validateDuplicatedReservationNot(reservationId, scheduleId);

        int affectedRow = reservationRepository.updateScheduleByIdAndMemberId(oldReservation.id(), memberId, scheduleId);
        validateReservationUpdated(affectedRow);

        return ReservationSaveResponse.from(getNewReservationOrThrow(reservationId, memberId));
    }

    private static void validateReservationUpdated(int affectedRow) {
        if (affectedRow != 1) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_UPDATE_FAILED);
        }
    }

    private static void validateNotEmptyUpdateRequest(ReservationUpdateRequest body) {
        if (body.date() == null && body.timeId() == null) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_UPDATE_EMPTY);
        }
    }

    private Reservation getNewReservationOrThrow(long reservationId, long memberId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND_AFTER_UPDATE, memberId, reservationId));
    }

    private ReservationDetailProjection getOldReservationDetailOrThrow(long reservationId, long memberId) {
        return reservationRepository.findDetailByIdAndMemberId(reservationId, memberId)
                .orElseThrow(() -> new EscapeRoomException(ErrorCode.RESERVATION_NOT_FOUND, memberId, reservationId));
    }

    private void validateDuplicatedReservationNot(long reservationId, long scheduleId) {
        if (reservationRepository.existsByScheduleIdAndIdNot(scheduleId, reservationId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_ALREADY_EXIST, scheduleId);
        }
    }

    private void validateReservationAlreadyExistsNot(long scheduleId) {
        if (reservationRepository.existsByScheduleId(scheduleId)) {
            throw new EscapeRoomException(ErrorCode.RESERVATION_ALREADY_EXIST, scheduleId);
        }
    }

    private void validateNotPast(ReservationDetailProjection reservationDetail) {
        scheduleService.validateNotPastDate(reservationDetail.date());
        scheduleService.validateNotPastTime(reservationDetail.date(), reservationDetail.getTime());
    }
}
