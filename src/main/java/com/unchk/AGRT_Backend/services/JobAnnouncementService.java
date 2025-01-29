package com.unchk.AGRT_Backend.services;

import com.unchk.AGRT_Backend.dto.JobAnnouncementDTO;

import java.util.List;
import java.util.UUID;

public interface JobAnnouncementService {
    JobAnnouncementDTO createAnnouncement(JobAnnouncementDTO announcementDTO);
    List<JobAnnouncementDTO> getAllAnnouncements();
    JobAnnouncementDTO getAnnouncementById(UUID id);
    JobAnnouncementDTO updateAnnouncement(UUID id, JobAnnouncementDTO announcementDTO);
    void deleteAnnouncement(UUID id);
}