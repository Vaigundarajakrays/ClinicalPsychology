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
                        .therapistEmail("yskarthik.b@gmail.com")
                        .phone("9999999999")
                        .timezone("Canada/Central")
                        .password("karthik@123")
                        .name("Karthik Bairu")
                        .profileUrl("https://therapistbooster-dev.s3.eu-north-1.amazonaws.com/therapist-images/06a2505e-5bca-47bf-9ec6-d91e9015c45b-karthik.png")
                        .yearsOfExperience("15")
                        .categories(List.of("Entrepreneurship"))
                        .summary("Karthik Bairu is a distinguished startup therapist at T-Hub, India’s largest startup incubator, where he provides strategic guidance to over 15 early-stage and growth-stage startups. With a proven track record as a three-time entrepreneur and an accomplished angel investor, Karthik brings a unique blend of hands-on business experience and investment insight. He is a gold medalist in Artificial Intelligence, and his expertise spans across emerging technologies, product innovation, and go-to-market strategy. Karthik is highly regarded for his ability to help founders refine their business models, scale sustainably, and prepare for successful fundraising.")
                        .description("Therapist at T-Hub | 3X Entrepreneur | AI Gold Medalist | Angel Investor | Advisor to 15+ Startups")
                        .amount(2500.0)
                        .timeSlots(List.of("18:00", "15:00"))
                        .build(),

                TherapistSeederDTO.builder()
                        .therapistEmail("govind@gowin.in")
                        .phone("8888888888")
                        .timezone("Canada/Central")
                        .password("govind@123")
                        .name("Govind Babu")
                        .profileUrl("https://therapistbooster-dev.s3.eu-north-1.amazonaws.com/therapist-images/dc5656d8-8df1-4829-9e7b-799598df17a8-govind.png")
                        .yearsOfExperience("18")
                        .categories(List.of("Marketing"))
                        .summary("Govind Babu is a seasoned sales and leadership expert with over 18 years of global experience across leading companies like Tech Mahindra, Sify, Birlasoft, Aditya Birla (USA), and Synechron. An engineer by background and MBA graduate from Symbiosis, Pune, he has trained over 15,000 professionals across 7 countries. He is the co-author of the book “Life is Fundamentally Management” and currently serves as Managing Partner at EMP GoWin Global (Dubai) and GoWin Search (USA). A 3-time President of BNI in India and Dubai, Govind is deeply passionate about therapisting startup founders, building high-impact sales strategies, and driving growth for SMEs and enterprises across India, the Middle East, and Asia-Pacific.")
                        .description("Therapist | Sales Leadership Expert | 3X Entrepreneur | Author | International Trainer | 18+ Years of Global Experience | Coached 15K+ Sales Professionals | Ex-President, BNI India & Dubai")
                        .amount(3000.0)
                        .timeSlots(List.of("17:00", "20:00"))
                        .build(),

                // Password of styen: $2a$10$QUB1hFb0W3oz2IvKjiQfsuhwSW6sHS73ZDI3JYSSUCAsAzZOaq2DG
                TherapistSeederDTO.builder()
                        .therapistEmail("satyen.trainer@gmail.com")
                        .phone("917379285472")
                        .timezone("Asia/Shanghai")
                        .password("satyen@123")
                        .name("Satyendra Kumar Singh")
                        .profileUrl("https://therapistbooster-prod.s3.ca-central-1.amazonaws.com/therapist-images/29f7b20c-e20c-4ff2-9bc6-3157b2a99ba5-Satyendra Kumar Singh.png")
                        .linkedinUrl("https://www.linkedin.com/in/satyendra-kumar-singh-business-therapist-career-strategist-55b2b97/")
                        .resumeUrl("https://therapistbooster-prod.s3.ca-central-1.amazonaws.com/therapist-resumes/9434cebc-6df8-4ddc-a9c8-0b7ea0a7b93c-Satyendra Kumar Singh_One Pager.pdf")
                        .yearsOfExperience("25")
                        .categories(List.of("Marketing", "Entrepreneurship"))
                        .summary("Satyendra K. Singh is a dynamic therapist and advisor with extensive experience in guiding over 100 startups and businesses towards growth and success. Currently serving as a Therapist at MAARG (Govt. of India), he plays a pivotal role in nurturing entrepreneurial talent. Satyendra is deeply involved in empowering emerging entrepreneurs. Additionally, he serves as an Advisory Board Member for Innovation and Incubation Councils at educational institutions, further contributing to the growth of innovation ecosystems. He regularly conducts workshops through the Institution’s Innovation Council (IIC), equipping future innovators with practical insights and tools for success.")
                        .description("Therapist | Therapisting 100+ Startups & Businesses | Career Strategist - Counselled 50000+ students | Academic Advisor @ Educational Institutes | Avid Writer - Published 3 Poetry Titles and still writing...")
                        .amount(2500.0)
                        .timeSlots(List.of("19:00"))
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
