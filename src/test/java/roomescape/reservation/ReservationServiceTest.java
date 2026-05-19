package roomescape.reservation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.exception.ErrorCode;
import roomescape.exception.EscapeRoomException;
import roomescape.member.Role;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.ReservationSaveResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.projection.ReservationDetailProjection;
import roomescape.schedule.ScheduleService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final long MEMBER_ID = 1L;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ScheduleService scheduleService;

    @InjectMocks
    private ReservationService reservationService;

    private ReservationDetailProjection createReservationDetailProjection(
            Long id,
            Long memberId,
            LocalDate date,
            Long themeId,
            Long timeId,
            LocalTime startAt
    ) {
        return new ReservationDetailProjection(
                id,
                memberId,
                "testMemberName",
                "testPassword",
                Role.USER,
                date,
                themeId,
                "themeName",
                "themeDescription",
                "themeThumbnailUrl",
                timeId,
                startAt
        );
    }

    @Test
    @DisplayName("회원 id 기준 예약 삭제에 성공한다.")
    void deleteByIdAndMemberId_성공_테스트() {
        long reservationId = 1L;
        ReservationDetailProjection reservationDetail = createReservationDetailProjection(
                reservationId, MEMBER_ID, LocalDate.of(2026, 5, 5), 1L, 7L, LocalTime.of(8, 0));
        when(reservationRepository.findDetailByIdAndMemberId(reservationId, MEMBER_ID))
                .thenReturn(Optional.of(reservationDetail));

        assertThatCode(() -> reservationService.deleteByIdAndMemberId(reservationId, MEMBER_ID))
                .doesNotThrowAnyException();

        verify(reservationRepository).deleteByIdAndMemberId(reservationId, MEMBER_ID);
    }

    @Test
    @DisplayName("없는 예약 삭제 요청은 성공 처리한다.")
    void deleteByIdAndMemberId_없는예약_테스트() {
        long reservationId = 999L;
        when(reservationRepository.findDetailByIdAndMemberId(reservationId, MEMBER_ID))
                .thenReturn(Optional.empty());

        assertThatCode(() -> reservationService.deleteByIdAndMemberId(reservationId, MEMBER_ID))
                .doesNotThrowAnyException();

        verify(reservationRepository, never()).deleteByIdAndMemberId(reservationId, MEMBER_ID);
    }

    @Test
    @DisplayName("예약 날짜가 과거인 경우 삭제 시도 시 예외가 발생한다.")
    void deleteByIdAndMemberId_과거날짜_테스트() {
        long reservationId = 1L;
        ReservationDetailProjection reservationDetail = createReservationDetailProjection(
                reservationId, MEMBER_ID, LocalDate.of(2026, 5, 13), 1L, 1L, LocalTime.of(10, 0));

        when(reservationRepository.findDetailByIdAndMemberId(reservationId, MEMBER_ID))
                .thenReturn(Optional.of(reservationDetail));
        doThrow(EscapeRoomException.class)
                .when(scheduleService)
                .validateNotPastDate(reservationDetail.date());

        assertThatThrownBy(() -> reservationService.deleteByIdAndMemberId(reservationId, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);

        verify(reservationRepository, never()).deleteByIdAndMemberId(reservationId, MEMBER_ID);
    }

    @Test
    @DisplayName("예약 시간이 과거인 경우 삭제 시도 시 예외가 발생한다.")
    void deleteByIdAndMemberId_과거시간_테스트() {
        long reservationId = 1L;
        ReservationDetailProjection reservationDetail = createReservationDetailProjection(
                reservationId, MEMBER_ID, LocalDate.of(2026, 5, 13), 1L, 1L, LocalTime.of(10, 0));

        when(reservationRepository.findDetailByIdAndMemberId(reservationId, MEMBER_ID))
                .thenReturn(Optional.of(reservationDetail));
        doThrow(EscapeRoomException.class)
                .when(scheduleService)
                .validateNotPastTime(reservationDetail.date(), reservationDetail.getTime());

        assertThatThrownBy(() -> reservationService.deleteByIdAndMemberId(reservationId, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);

        verify(reservationRepository, never()).deleteByIdAndMemberId(reservationId, MEMBER_ID);
    }

    @Test
    @DisplayName("스케줄이 존재하고 해당 스케줄에 예약이 없다면 예약을 변경할 수 있다.")
    void update_성공_테스트() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(
                LocalDate.of(2026, 5, 5),
                4L
        );
        ReservationDetailProjection oldReservation = createReservationDetailProjection(
                4L, MEMBER_ID, LocalDate.of(2026, 5, 5), 4L, 3L, LocalTime.of(12, 0));
        Reservation updatedReservation = new Reservation(4L, MEMBER_ID, 4L);

        when(reservationRepository.findDetailByIdAndMemberId(4L, MEMBER_ID))
                .thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(LocalDate.of(2026, 5, 5), 4L, 4L))
                .thenReturn(4L);
        when(reservationRepository.existsByScheduleIdAndIdNot(4L, 4L)).thenReturn(false);
        when(reservationRepository.updateScheduleByIdAndMemberId(4L, MEMBER_ID, 4L)).thenReturn(1);
        when(reservationRepository.findById(4L)).thenReturn(Optional.of(updatedReservation));

        ReservationSaveResponse response = reservationService.update(request, 4L, MEMBER_ID);

        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.memberId()).isEqualTo(MEMBER_ID);
        assertThat(response.scheduleId()).isEqualTo(4L);
        verify(reservationRepository).updateScheduleByIdAndMemberId(4L, MEMBER_ID, 4L);
    }

    @Test
    @DisplayName("변경하려는 스케줄에 다른 예약이 있을 경우 예외가 발생한다.")
    void update_중복스케줄_실패_테스트() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(
                LocalDate.of(2026, 5, 5),
                2L
        );
        ReservationDetailProjection oldReservation = createReservationDetailProjection(
                4L, MEMBER_ID, LocalDate.of(2026, 5, 6), 2L, 2L, LocalTime.of(11, 0));

        when(reservationRepository.findDetailByIdAndMemberId(4L, MEMBER_ID))
                .thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(LocalDate.of(2026, 5, 5), 2L, 2L))
                .thenReturn(2L);
        when(reservationRepository.existsByScheduleIdAndIdNot(2L, 4L)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.update(request, 4L, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).updateScheduleByIdAndMemberId(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("본인 예약이 아닌데 변경을 시도하면 예외가 발생한다.")
    void update_본인예약아님_실패_테스트() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(
                LocalDate.of(2026, 5, 5),
                4L
        );
        when(reservationRepository.findDetailByIdAndMemberId(4L, MEMBER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.update(request, 4L, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).updateScheduleByIdAndMemberId(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("없는 스케줄로 예약을 변경하려고 시도할 경우 예외가 발생한다.")
    void update_없는스케줄_실패_테스트() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(
                LocalDate.of(2026, 5, 7),
                5L
        );
        ReservationDetailProjection oldReservation = createReservationDetailProjection(
                4L, MEMBER_ID, LocalDate.of(2026, 5, 6), 2L, 2L, LocalTime.of(11, 0));

        when(reservationRepository.findDetailByIdAndMemberId(4L, MEMBER_ID))
                .thenReturn(Optional.of(oldReservation));
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(LocalDate.of(2026, 5, 7), 5L, 2L))
                .thenThrow(new IllegalArgumentException("해당 조건을 가진 일정이 없습니다."));

        assertThatThrownBy(() -> reservationService.update(request, 4L, MEMBER_ID))
                .isInstanceOf(IllegalArgumentException.class);
        verify(reservationRepository, never()).updateScheduleByIdAndMemberId(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("과거 날짜의 예약을 변경하려 할 경우 변경에 실패한다.")
    void update_과거날짜_실패_테스트() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(
                LocalDate.of(2026, 5, 7),
                5L
        );
        ReservationDetailProjection oldReservation = createReservationDetailProjection(
                4L, MEMBER_ID, LocalDate.of(2026, 5, 6), 2L, 2L, LocalTime.of(11, 0));

        when(reservationRepository.findDetailByIdAndMemberId(4L, MEMBER_ID))
                .thenReturn(Optional.of(oldReservation));
        doThrow(IllegalStateException.class).when(scheduleService).validateNotPastDate(oldReservation.date());

        assertThatThrownBy(() -> reservationService.update(request, 4L, MEMBER_ID))
                .isInstanceOf(IllegalStateException.class);

        verify(scheduleService).validateNotPastDate(oldReservation.date());
        verify(scheduleService, never()).validateNotPastTime(any(LocalDate.class), any(LocalTime.class));
        verify(scheduleService, never()).findScheduleIdByDateAndTimeIdAndThemeId(any(LocalDate.class), anyLong(), anyLong());
        verify(reservationRepository, never()).updateScheduleByIdAndMemberId(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("예약 시간이 과거인 경우 변경에 실패한다.")
    void update_과거시간_실패_테스트() {
        ReservationUpdateRequest request = new ReservationUpdateRequest(
                LocalDate.of(2026, 5, 7),
                5L
        );
        ReservationDetailProjection oldReservation = createReservationDetailProjection(
                4L, MEMBER_ID, LocalDate.of(2026, 5, 6), 2L, 2L, LocalTime.of(11, 0));

        when(reservationRepository.findDetailByIdAndMemberId(4L, MEMBER_ID))
                .thenReturn(Optional.of(oldReservation));
        doNothing().when(scheduleService).validateNotPastDate(oldReservation.date());
        doThrow(IllegalStateException.class).when(scheduleService).validateNotPastTime(oldReservation.date(), oldReservation.getTime());

        assertThatThrownBy(() -> reservationService.update(request, 4L, MEMBER_ID))
                .isInstanceOf(IllegalStateException.class);

        verify(scheduleService).validateNotPastDate(oldReservation.date());
        verify(scheduleService).validateNotPastTime(oldReservation.date(), oldReservation.getTime());
        verify(scheduleService, never()).findScheduleIdByDateAndTimeIdAndThemeId(any(LocalDate.class), anyLong(), anyLong());
        verify(reservationRepository, never()).updateScheduleByIdAndMemberId(anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("예약 저장 성공 테스트")
    void save_성공_테스트() {
        ReservationSaveRequest body = new ReservationSaveRequest(
                LocalDate.of(2026, 5, 5),
                4L,
                4L
        );
        long scheduleId = 4L;
        Reservation savedReservation = new Reservation(5L, MEMBER_ID, scheduleId);

        doNothing().when(scheduleService).validateSchedule(body.date(), body.timeId(), body.themeId());
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(body.date(), body.timeId(), body.themeId()))
                .thenReturn(scheduleId);
        when(reservationRepository.existsByScheduleId(scheduleId)).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReservationSaveResponse response = reservationService.save(body, MEMBER_ID);

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.memberId()).isEqualTo(MEMBER_ID);
        assertThat(response.scheduleId()).isEqualTo(4L);
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("예약 요청이 들어왔을 때 스케줄 검증이 되지 않는다면 예외가 발생한다.")
    void save_스케줄검증_실패_테스트() {
        ReservationSaveRequest body = new ReservationSaveRequest(
                LocalDate.of(2026, 5, 5),
                4L,
                4L
        );
        doThrow(new EscapeRoomException(ErrorCode.PAST_SCHEDULE))
                .when(scheduleService)
                .validateSchedule(body.date(), body.timeId(), body.themeId());

        assertThatThrownBy(() -> reservationService.save(body, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("특정 스케줄을 가진 다른 예약이 존재할 경우 예외가 발생한다.")
    void save_중복스케줄_실패_테스트() {
        ReservationSaveRequest body = new ReservationSaveRequest(
                LocalDate.of(2026, 5, 5),
                1L,
                1L
        );
        long scheduleId = 1L;

        doNothing().when(scheduleService).validateSchedule(body.date(), body.timeId(), body.themeId());
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(body.date(), body.timeId(), body.themeId()))
                .thenReturn(scheduleId);
        when(reservationRepository.existsByScheduleId(scheduleId)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.save(body, MEMBER_ID))
                .isInstanceOf(EscapeRoomException.class);

        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    @DisplayName("특정 스케줄이 존재하지 않는다면 예외가 발생한다.")
    void save_스케줄없음_실패_테스트() {
        ReservationSaveRequest body = new ReservationSaveRequest(
                LocalDate.of(2026, 5, 11),
                99L,
                99L
        );

        doNothing().when(scheduleService).validateSchedule(body.date(), body.timeId(), body.themeId());
        when(scheduleService.findScheduleIdByDateAndTimeIdAndThemeId(body.date(), body.timeId(), body.themeId()))
                .thenThrow(IllegalStateException.class);

        assertThatThrownBy(() -> reservationService.save(body, MEMBER_ID))
                .isInstanceOf(IllegalStateException.class);

        verify(reservationRepository, never()).existsByScheduleId(anyLong());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
