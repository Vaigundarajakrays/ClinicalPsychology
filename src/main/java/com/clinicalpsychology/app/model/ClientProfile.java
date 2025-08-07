package com.clinicalpsychology.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
public class ClientProfile extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String description;

    private List<String> goals;

    private String timeZone;

    private String gender;

    private String dob;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String summary;

//    @Column(nullable = false)
    private String profileUrl;

    private List<String> languages;

    private String subscriptionPlan;

    private String industry;

    private String location;

    private String status;

}
