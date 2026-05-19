package roomescape.reservation.repository;

import roomescape.reservation.Reservation;
import roomescape.reservation.repository.projection.ReservationDetailProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    List<ReservationDetailProjection> findAllDetails();

    Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    List<ReservationDetailProjection> findAllReservationDetailsByMemberId(long memberId);

    void deleteByIdAndMemberId(long reservationId, long memberId);

    Optional<ReservationDetailProjection> findDetailByIdAndMemberId(long reservationId, long memberId);

    boolean existsByScheduleIdAndIdNot(long scheduleId, long reservationId);

    int updateScheduleByIdAndMemberId(long reservationId, long memberId, long scheduleId);

    Optional<Reservation> findById(long reservationId);

    boolean existsByScheduleId(long scheduleId);
}
