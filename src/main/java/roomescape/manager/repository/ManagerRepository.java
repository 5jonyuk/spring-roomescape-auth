package roomescape.manager.repository;

import roomescape.manager.Manager;

import java.util.Optional;

public interface ManagerRepository {
    Optional<Manager> findByMemberId(long memberId);
}
