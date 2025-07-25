package com.clinicalpsychology.app.seed;

import com.clinicalpsychology.app.enumUtil.AccountStatus;
import com.clinicalpsychology.app.enumUtil.ApprovalStatus;
import com.clinicalpsychology.app.enumUtil.Role;
import com.clinicalpsychology.app.model.FixedTimeSlotNew;
import com.clinicalpsychology.app.model.TherapistProfile;
import com.clinicalpsychology.app.model.Users;
import com.clinicalpsychology.app.repository.ClientProfileRepository;
import com.clinicalpsychology.app.repository.TherapistProfileRepository;
import com.clinicalpsychology.app.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TherapistSeeder implements CommandLineRunner {

    private final TherapistProfileRepository therapistProfileRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientProfileRepository clientProfileRepository;

    @Override
    public void run(String... args) {

        List<TherapistSeederDTO> therapistsToSeed = List.of(
                TherapistSeederDTO.builder()
                        .therapistEmail("sarah@gmail.com")
                        .phone("9999999999")
                        .timezone("Canada/Central")
                        .password("sarah@123")
                        .name("Dr. Sarah Johnson")
                        .profileUrl("https://mentorbooster-prod.s3.ca-central-1.amazonaws.com/mentor-images/2a8956fb-fd9b-4187-afc3-2f0d635ef34c-therapist-1.webp")
                        .yearsOfExperience("8")
                        .categories(List.of("Anxiety", "Depression", "CBT"))
                        .summary("Dr. Sarah Johnson is a licensed clinical psychologist with over 8 years of experience in treating anxiety disorders, depression, and trauma. She specializes in Cognitive Behavioral Therapy (CBT) and has helped hundreds of clients overcome their mental health challenges. Dr. Johnson received her Ph.D. in Clinical Psychology from Columbia University and completed her internship at Mount Sinai Hospital.")
                        .description("Specializing in cognitive behavioral therapy with a focus on anxiety and depression. I believe in creating a safe, non-judgmental space where clients can explore their thoughts and emotions.")
                        .amount(2500.0)
                        .timeSlots(List.of("18:00", "15:00"))
                        .build(),

                TherapistSeederDTO.builder()
                        .therapistEmail("michael@gmail.com")
                        .phone("8888888888")
                        .timezone("Canada/Central")
                        .password("michael@123")
                        .name("Dr. Michael Chen")
                        .profileUrl("https://mentorbooster-prod.s3.ca-central-1.amazonaws.com/mentor-images/b4ec3d7a-fbf5-4e78-b339-6fe077bc019e-therapist-2.webp")
                        .yearsOfExperience("12")
                        .categories(List.of("Trauma", "PTSD", "EMDR"))
                        .summary("Dr. Michael Chen is a highly respected trauma and PTSD specialist with over 12 years of clinical experience across North America. Holding advanced certifications in EMDR and trauma-informed therapy, Dr. Chen has worked extensively with individuals, veterans, and corporate professionals dealing with post-traumatic stress and emotional resilience. He is deeply rooted in evidence-based practices and combines neuroscience with compassionate therapy to enable deep healing. A graduate in Clinical Psychology and trained across leading mental health institutions in Canada and the U.S., Dr. Chen brings a unique East-meets-West approach to his sessions.")
                        .description("Licensed clinical psychologist with extensive experience in trauma therapy and EMDR. I work with individuals who have experienced various forms of hardships")
                        .amount(3000.0)
                        .timeSlots(List.of("17:00", "20:00"))
                        .build(),

                TherapistSeederDTO.builder()
                        .therapistEmail("aisha.williams@gmail.com")
                        .phone("7777777777")
                        .timezone("America/New_York")
                        .password("aisha@123")
                        .name("Dr. Aisha Williams")
                        .profileUrl("https://mentorbooster-prod.s3.ca-central-1.amazonaws.com/mentor-images/ee7d0070-355c-45fb-9587-80007cacb184-therapist-3.webp")
                        .yearsOfExperience("15")
                        .categories(List.of("Anxiety", "Women’s Mental Health", "Mindfulness"))
                        .summary("Dr. Aisha Williams is a renowned clinical psychologist with 15 years of experience specializing in anxiety management, women’s mental health, and mindfulness-based cognitive therapy. With a doctorate in Psychology from Columbia University, Dr. Williams has empowered thousands of individuals—especially women navigating transitions, burnout, or high-functioning anxiety—to reclaim their calm and confidence. Her work integrates mindfulness, somatic practices, and deep cognitive restructuring to foster emotional well-being and self-awareness. Based in New York, she offers a warm, judgment-free therapeutic space, blending science-backed strategies with soulful presence.")
                        .description("Board-certified clinical psychologist passionate about helping women and professionals manage anxiety, stress, and emotional overload through mindfulness, talk therapy, and empowerment strategies.")
                        .amount(3500.0)
                        .timeSlots(List.of("09:00", "13:00", "18:30"))
                        .build()

                );

        for (TherapistSeederDTO dto : therapistsToSeed) {
            if (therapistProfileRepository.existsByEmailOrPhone(dto.getTherapistEmail(), dto.getPhone())) {
                log.warn("⚠️ Therapist with email {} or phone {} already exists. Skipping seeding.", dto.getTherapistEmail(), dto.getPhone());
                continue;
            }
            if (clientProfileRepository.existsByEmail(dto.getTherapistEmail())){
                log.warn("⚠️ Therapist with email {} already registered as client. So skipped seeding.", dto.getTherapistEmail());
                continue;
            }

            try {
                ZoneId zoneId = ZoneId.of(dto.getTimezone());
                LocalDate today = LocalDate.now(zoneId);
                String hashedPassword = passwordEncoder.encode(dto.getPassword());

                String defaultLinkedinUrl = "https://linkedin.com/in/sampleprofile";
                String defaultResumeUrl = "https://therapistbooster-resumes.s3.amazonaws.com/sample_resume.pdf";
                if(dto.getResumeUrl() != null && dto.getLinkedinUrl() != null){
                    defaultLinkedinUrl = dto.getLinkedinUrl();
                    defaultResumeUrl = dto.getResumeUrl();
                }

                TherapistProfile therapist = TherapistProfile.builder()
                        .name(dto.getName())
                        .email(dto.getTherapistEmail())
                        .phone(dto.getPhone())
                        .linkedinUrl(defaultLinkedinUrl) // 🔧 placeholder
                        .profileUrl(dto.getProfileUrl())
                        .resumeUrl(defaultResumeUrl)
                        .yearsOfExperience(dto.getYearsOfExperience())
                        .password(hashedPassword)
                        .categories(dto.getCategories())
                        .summary(dto.getSummary())
                        .description(dto.getDescription())
                        .amount(dto.getAmount())
                        .terms(true)
                        .termsAndConditions(true)
                        .location("Chennai")
                        .timezone(dto.getTimezone())
                        .accountStatus(AccountStatus.ACTIVE)
                        .approvalStatus(ApprovalStatus.ACCEPTED)
                        .build();

                List<FixedTimeSlotNew> timeSlots = dto.getTimeSlots().stream().map(timeStr -> {
                    LocalTime localTime = LocalTime.parse(timeStr.trim());
                    ZonedDateTime zdt = ZonedDateTime.of(today, localTime, zoneId);
                    return FixedTimeSlotNew.builder()
                            .timeStart(zdt.toInstant())
                            .therapist(therapist)
                            .build();
                }).toList();

                therapist.setTimeSlots(timeSlots);
                therapistProfileRepository.save(therapist);

                Users user = Users.builder()
                        .emailId(dto.getTherapistEmail())
                        .password(hashedPassword)
                        .role(Role.THERAPIST)
                        .build();
                usersRepository.save(user);

                log.info("✅ Seeded therapist: {}", dto.getName());

            } catch (Exception e) {
                log.error("❌ Failed to seed therapist {}: {}", dto.getName(), e.getMessage(), e);
            }
        }
    }
}
