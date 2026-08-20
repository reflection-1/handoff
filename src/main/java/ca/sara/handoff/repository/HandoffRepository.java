package ca.sara.handoff.repository;

import ca.sara.handoff.domain.Handoff;
import ca.sara.handoff.domain.HandoffStatus;
import ca.sara.handoff.domain.Priority;
import ca.sara.handoff.domain.ShiftType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HandoffRepository extends JpaRepository<Handoff, Long> {

    @Query("""
            select h from Handoff h
            where (:status is null or h.status = :status)
              and (:priority is null or h.priority = :priority)
              and (:shiftType is null or h.shiftType = :shiftType)
              and (:query is null
                   or lower(h.title) like lower(concat('%', :query, '%'))
                   or lower(h.details) like lower(concat('%', :query, '%'))
                   or lower(h.area) like lower(concat('%', :query, '%'))
                   or lower(h.owner) like lower(concat('%', :query, '%')))
            order by h.updatedAt desc
            """)
    List<Handoff> search(
            @Param("status") HandoffStatus status,
            @Param("priority") Priority priority,
            @Param("shiftType") ShiftType shiftType,
            @Param("query") String query
    );
}
