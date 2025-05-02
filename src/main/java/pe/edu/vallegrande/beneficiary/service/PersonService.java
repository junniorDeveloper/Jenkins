package pe.edu.vallegrande.beneficiary.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.beneficiary.dto.PersonDTO;
import pe.edu.vallegrande.beneficiary.dto.HealthDTO;
import pe.edu.vallegrande.beneficiary.dto.EducationDTO;
import pe.edu.vallegrande.beneficiary.model.Person;
import pe.edu.vallegrande.beneficiary.model.Education;
import pe.edu.vallegrande.beneficiary.model.Health;
import pe.edu.vallegrande.beneficiary.repository.EducationRepository;
import pe.edu.vallegrande.beneficiary.repository.HealthRepository;
import pe.edu.vallegrande.beneficiary.repository.PersonRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private HealthRepository healthRepository;

    //LISTADO DE BENEFICIARIOS ACTIVOS Y INACTIVOS
    public Flux<PersonDTO> getPersonsByTypeKinshipAndState(String typeKinship, String state) {
        return personRepository.findByTypeKinshipAndState(typeKinship, state)
                .map(this::convertToDTO);
    }

    //LISTADO DE APADRINADOS ACTIVOS Y INACTIVOS
    public Flux<PersonDTO> getPersonsBySponsoredAndState(String sponsored, String state) {
        return personRepository.findBySponsoredAndState(sponsored, state)
                .map(this::convertToDTO);
    }

    //LISTADO COMPLETOS DE BENEFICIARIOS POR ID
     public Mono<PersonDTO> getPersonByIdWithDetails(Integer id) {
        return personRepository.findById(id)
                .flatMap(person -> {
                    PersonDTO dto = convertToDTO(person);
                    return educationRepository.findByPersonId(person.getIdPerson())
                            .collectList()
                            .flatMap(educationList -> {
                                // Convierte la lista de Education a EducationDTO
                                List<EducationDTO> educationDTOList = educationList.stream()
                                        .map(this::convertToEducationDTO)
                                        .collect(Collectors.toList());
                                dto.setEducation(educationDTOList);
                                return healthRepository.findByPersonId(person.getIdPerson())
                                        .collectList()
                                        .map(healthList -> {
                                            // Convierte la lista de Health a HealthDTO
                                            List<HealthDTO> healthDTOList = healthList.stream()
                                                    .map(this::convertToHealthDTO)
                                                    .collect(Collectors.toList());
                                            dto.setHealth(healthDTOList);
                                            return dto;
                                        });
                            });
                });
    }

    //ELIMINADO LOGICO
    public Mono<Void> deletePerson(Integer id) {
        return personRepository.updateStateById(id, "I")
                .then();
    }

    //RESTAURADO LOGICO
    public Mono<Void> restorePerson(Integer id) {
        return personRepository.updateStateById(id, "A")
                .then();
    }

     //ACTUALIZA LOS REGISTROS DE EDUCATION Y HEALTH CON NUEVO IDS
    public Mono<Void> updatePersonWithNewIds(PersonDTO personDTO) {
        return Mono.when(
                personRepository.updateStateById(personDTO.getIdPerson(), personDTO.getState()),
    
                // Verificar si se proporcionaron datos de education
                personDTO.getEducation() != null && !personDTO.getEducation().isEmpty() ?
                        educationRepository.insertEducation(
                                personDTO.getEducation().get(0).getDegreeStudy(),
                                personDTO.getEducation().get(0).getGradeBook(),
                                personDTO.getEducation().get(0).getGradeAverage(),
                                personDTO.getEducation().get(0).getFullNotebook(),
                                personDTO.getEducation().get(0).getAssistance(),
                                personDTO.getEducation().get(0).getTutorials(),
                                personDTO.getIdPerson()
                        ) : Mono.empty(),
    
                // Verificar si se proporcionaron datos de health
                personDTO.getHealth() != null && !personDTO.getHealth().isEmpty() ?
                        healthRepository.insertHealth(
                                personDTO.getHealth().get(0).getVaccine(),
                                personDTO.getHealth().get(0).getVph(),
                                personDTO.getHealth().get(0).getInfluenza(),
                                personDTO.getHealth().get(0).getDeworming(),
                                personDTO.getHealth().get(0).getHemoglobin(),
                                personDTO.getIdPerson()
                        ) : Mono.empty()
        ).then();
    }

    //EDITAR DATOS PERSONALES SIN GENERAR NUEVO ID
    public Mono<Void> updatePersonData(PersonDTO personDTO) {
        return personRepository.updatePerson(
                personDTO.getIdPerson(),
                personDTO.getName(),
                personDTO.getSurname(),
                personDTO.getAge(),
                personDTO.getBirthdate(),
                personDTO.getTypeDocument(),
                personDTO.getDocumentNumber(),
                personDTO.getTypeKinship(),
                personDTO.getSponsored(),
                personDTO.getState(),
                personDTO.getFamilyId()
        ).then();
    }

    //MODIFICA EDUCATION Y HEALT SIN GENERAR UN NUEVO ID
    public Mono<Void> correctEducationAndHealth(PersonDTO personDTO) {
        return Mono.when(
                // Verificar si se proporcionaron datos de education y actualizar
                personDTO.getEducation() != null && !personDTO.getEducation().isEmpty() ?
                        educationRepository.updateEducation(
                                personDTO.getEducation().get(0).getIdEducation(),
                                personDTO.getEducation().get(0).getDegreeStudy(),
                                personDTO.getEducation().get(0).getGradeBook(),
                                personDTO.getEducation().get(0).getGradeAverage(),
                                personDTO.getEducation().get(0).getFullNotebook(),
                                personDTO.getEducation().get(0).getAssistance(),
                                personDTO.getEducation().get(0).getTutorials()
                        ) : Mono.empty(),
    
                // Verificar si se proporcionaron datos de health y actualizar
                personDTO.getHealth() != null && !personDTO.getHealth().isEmpty() ?
                        healthRepository.updateHealth(
                                personDTO.getHealth().get(0).getIdHealth(),
                                personDTO.getHealth().get(0).getVaccine(),
                                personDTO.getHealth().get(0).getVph(),
                                personDTO.getHealth().get(0).getInfluenza(),
                                personDTO.getHealth().get(0).getDeworming(),
                                personDTO.getHealth().get(0).getHemoglobin()
                        ) : Mono.empty()
        ).then();
    }

    //REGISTRA NUEVA PERSONA CON SUS DATOS DE EDUCATION Y HEALTH
    public Mono<Void> registerPerson(PersonDTO personDTO) {
        return personRepository.insertPerson(
                personDTO.getName(),
                personDTO.getSurname(),
                personDTO.getAge(),
                personDTO.getBirthdate(),
                personDTO.getTypeDocument(),
                personDTO.getDocumentNumber(),
                personDTO.getTypeKinship(),
                personDTO.getSponsored(),
                personDTO.getState(),
                personDTO.getFamilyId()
        ).then(personRepository.getLastInsertedId())  // 🔹 Obtener el ID después de la inserción
        .flatMap(personId -> 
            Flux.concat(
                Flux.fromIterable(personDTO.getEducation())
                    .flatMap(edu -> educationRepository.insertEducation(
                        edu.getDegreeStudy(), 
                        edu.getGradeBook(), 
                        edu.getGradeAverage(),
                        edu.getFullNotebook(), 
                        edu.getAssistance(), 
                        edu.getTutorials(), 
                        personId
                    )),
                Flux.fromIterable(personDTO.getHealth())
                    .flatMap(health -> healthRepository.insertHealth(
                        health.getVaccine(), 
                        health.getVph(), 
                        health.getInfluenza(),
                        health.getDeworming(), 
                        health.getHemoglobin(), 
                        personId
                    ))
            ).then()
        );
    }
    


    private PersonDTO convertToDTO(Person person) {
        PersonDTO dto = new PersonDTO();
        dto.setIdPerson(person.getIdPerson());
        dto.setName(person.getName());
        dto.setSurname(person.getSurname());
        dto.setAge(person.getAge());
        dto.setBirthdate(person.getBirthdate());
        dto.setTypeDocument(person.getTypeDocument());
        dto.setDocumentNumber(person.getDocumentNumber());
        dto.setTypeKinship(person.getTypeKinship());
        dto.setSponsored(person.getSponsored());
        dto.setState(person.getState());
        dto.setFamilyId(person.getFamilyId());
        return dto;
    }

    private EducationDTO convertToEducationDTO(Education education) {
        EducationDTO dto = new EducationDTO();
        dto.setIdEducation(education.getIdEducation());
        dto.setDegreeStudy(education.getDegreeStudy());
        dto.setGradeBook(education.getGradeBook());
        dto.setGradeAverage(education.getGradeAverage());
        dto.setFullNotebook(education.getFullNotebook());
        dto.setAssistance(education.getAssistance());
        dto.setTutorials(education.getTutorials());
        dto.setPersonId(education.getPersonId());
        return dto;
    }

    private HealthDTO convertToHealthDTO(Health health) {
        HealthDTO dto = new HealthDTO();
        dto.setIdHealth(health.getIdHealth());
        dto.setVaccine(health.getVaccine());
        dto.setVph(health.getVph());
        dto.setInfluenza(health.getInfluenza());
        dto.setDeworming(health.getDeworming());
        dto.setHemoglobin(health.getHemoglobin());
        dto.setPersonId(health.getPersonId());
        return dto;
    }
}