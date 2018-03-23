package pt.ist.renates.domain.thesis;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.fenixedu.academic.domain.Degree;
import org.fenixedu.academic.domain.DegreeCurricularPlan;
import org.fenixedu.academic.domain.Enrolment;
import org.fenixedu.academic.domain.Person;
import org.fenixedu.academic.domain.QueueJobResult;
import org.fenixedu.academic.domain.QueueJobWithFile;
import org.fenixedu.academic.domain.contacts.EmailAddress;
import org.fenixedu.academic.domain.degreeStructure.CycleCourseGroup;
import org.fenixedu.academic.domain.person.Gender;
import org.fenixedu.academic.domain.person.IDDocumentType;
import org.fenixedu.academic.domain.student.curriculum.ConclusionProcess;
import org.fenixedu.academic.domain.studentCurriculum.CurriculumGroup;
import org.fenixedu.academic.domain.studentCurriculum.CurriculumLine;
import org.fenixedu.academic.domain.studentCurriculum.Dismissal;
import org.fenixedu.academic.domain.studentCurriculum.Substitution;
import org.fenixedu.academic.domain.thesis.Thesis;
import org.fenixedu.academic.domain.thesis.ThesisEvaluationParticipant;
import org.fenixedu.academic.domain.thesis.ThesisParticipationType;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.commons.spreadsheet.Spreadsheet;
import org.fenixedu.commons.spreadsheet.Spreadsheet.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.renates.domain.InstitutionCodeProvider;
import pt.ist.renates.domain.RenatesIntegrationConfiguration;

public class ThesisRenatesReportFile extends QueueJobWithFile {

    private static final Logger logger = LoggerFactory.getLogger(ThesisRenatesReportFile.class);

    private final static Locale PT = new Locale("pt", "PT");

    protected ThesisRenatesReportFile() {
        super();
    }

    @Override
    public QueueJobResult execute() throws Exception {
        logger.debug("running {}", this.getExternalId());
        Spreadsheet spreasheet = retrieveIndividualThesisData();
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();

        spreasheet.exportToXLSSheet(byteArrayOS);
        byteArrayOS.close();

        final QueueJobResult queueJobResult = new QueueJobResult();
        queueJobResult.setContentType("application/excel");
        queueJobResult.setContent(byteArrayOS.toByteArray());

        return queueJobResult;
    }

    @Override
    public String getFilename() {
        return String.format("%s_%s.xls", "thesis_information", getRequestDate().toString("dd_MM_yyyy_hh_mm_ss"));
    }

    public static ThesisRenatesReportFile buildRenatesReport() {
        return new ThesisRenatesReportFile();
    }

    private Spreadsheet retrieveIndividualThesisData() {
        Spreadsheet spreadsheet = new Spreadsheet("sheet");

        spreadsheet.setHeader("NomeCompletooo"); //0
        spreadsheet.setHeader("Sexo"); //1
        spreadsheet.setHeader("DataNascimento"); //2
        spreadsheet.setHeader("NumIdentificacao"); //3
        spreadsheet.setHeader("TipoIdentificacao"); //4
        spreadsheet.setHeader("OutroTipoIdentificacao"); //5
        spreadsheet.setHeader("PaisNacionalidade"); //6
        spreadsheet.setHeader("OutraNacionalidade"); //Optional
        spreadsheet.setHeader("Morada"); //Optional
        spreadsheet.setHeader("Telefone");//Optional
        spreadsheet.setHeader("Email"); //10
        spreadsheet.setHeader("OutroEmail"); //Optional
        spreadsheet.setHeader("CodEstabelecimentoPortugues"); //12
        spreadsheet.setHeader("codUnidadeOrganica"); //13
        spreadsheet.setHeader("CodCurso"); //14
        spreadsheet.setHeader("Curso"); //15
        spreadsheet.setHeader("CodEspecializacao"); //16
        spreadsheet.setHeader("Especializacao"); //17
        spreadsheet.setHeader("TituloTrabalho"); //18
        spreadsheet.setHeader("NomeOrientador1"); //19
        spreadsheet.setHeader("NumIdentificacao_Orientador1"); //20
        spreadsheet.setHeader("TipoIdentificacao_Orientador1"); //21
        spreadsheet.setHeader("OutroTipoIdentificacao_Orientador1"); //22
        spreadsheet.setHeader("ORCID_Orientador1"); //Optional
        spreadsheet.setHeader("NomeOrientador2"); //Optional
        spreadsheet.setHeader("NumIdentificacao_Orientador2"); //Optional
        spreadsheet.setHeader("TipoIdentificacao_Orientador2"); //Optional
        spreadsheet.setHeader("OutroTipoIdentificacao_Orientador2"); //Optional
        spreadsheet.setHeader("ORCID_Orientador2"); //Optional
        spreadsheet.setHeader("NomeOrientador3"); //Optional
        spreadsheet.setHeader("NumIdentificacao_Orientador3"); //Optional
        spreadsheet.setHeader("TipoIdentificacao_Orientador3"); //Optional
        spreadsheet.setHeader("OutroTipoIdentificacao_Orientador3"); //Optional
        spreadsheet.setHeader("ORCID_Orientador3"); //Optional
        spreadsheet.setHeader("NomeOrientador4"); //Optional
        spreadsheet.setHeader("NumIdentificacao_Orientador4"); //Optional
        spreadsheet.setHeader("TipoIdentificacao_Orientador4"); //Optional
        spreadsheet.setHeader("OutroTipoIdentificacao_Orientador4"); //Optional
        spreadsheet.setHeader("ORCID_Orientador4"); //Optional
        spreadsheet.setHeader("NomeOrientador5"); //Optional
        spreadsheet.setHeader("NumIdentificacao_Orientador5"); //Optional
        spreadsheet.setHeader("TipoIdentificacao_Orientador5"); //Optional
        spreadsheet.setHeader("OutroTipoIdentificacao_Orientador5"); //Optional
        spreadsheet.setHeader("ORCID_Orientador5"); //Optional
        spreadsheet.setHeader("Co_orientador"); //Optional
        spreadsheet.setHeader("Palavras_chave"); //45
        spreadsheet.setHeader("Curso_Parceria"); //46
        spreadsheet.setHeader("Estabelecimento1"); //47
        spreadsheet.setHeader("Estabelecimento2"); //Optional
        spreadsheet.setHeader("Estabelecimento3"); //Optional
        spreadsheet.setHeader("Estabelecimento4"); //Optional
        spreadsheet.setHeader("Estabelecimento5"); //Optional
        spreadsheet.setHeader("Estabelecimento6"); //Optional
        spreadsheet.setHeader("Estabelecimento7"); //Optional
        spreadsheet.setHeader("Estabelecimento8"); //Optional
        spreadsheet.setHeader("Observacoes"); //Optional         
        spreadsheet.setHeader("DataGrauEmPortugal"); //56
        spreadsheet.setHeader("ClassificacaoID"); //57
        spreadsheet.setHeader("HandleDepositoRCAAP"); //Optional
        spreadsheet.setHeader("RegistoInternoID"); //Optional

        Set<CurriculumGroup> collect =
                Bennu.getInstance().getProgramConclusionSet().stream().flatMap(pc -> pc.getCourseGroupSet().stream())
                        .filter(cg -> cg.isCycleCourseGroup() && ((CycleCourseGroup) cg).isSecondCycle())
                        .flatMap(cg -> cg.getCurriculumModulesSet().stream()).map(cm -> (CurriculumGroup) cm)
                        .filter(cm -> cm.isConclusionProcessed()).collect(Collectors.toSet());

        for (CurriculumGroup group : collect) {
            Enrolment thesisEnrolment = getThesisEnrolment(group);
            if (thesisEnrolment == null) {
                continue;
            }
            Thesis thesis = thesisEnrolment.getPossibleThesis();
            ConclusionProcess conclusionProcess = group.getConclusionProcess();

            if (thesis == null) {
                continue;
            }
            if (thesis.getTid() != null) {
                continue;
            }

            final Row row = spreadsheet.addRow();

            final Person person = thesis.getStudent().getPerson();

            row.setCell(0, person.getName()); //NomeCompleto
            row.setCell(1, person.getGender() == Gender.MALE ? "H" : "F"); //Sexo
            row.setCell(2, person.getDateOfBirthYearMonthDay().toString("dd-MM-yyyy")); //DataNascimento
            row.setCell(3, person.getDocumentIdNumber()); //NumIdentificacao

            final String studentIdType = idDocumentTypeToNumber(person.getIdDocumentType());

            row.setCell(4, studentIdType); //TipoIdentificacao
            row.setCell(5, studentIdType.equals("9") ? person.getIdDocumentType().getLocalizedName(PT) : ""); //OutroTipoIdentificacao
            row.setCell(6, person.getCountryOfBirth().getCode()); //PaisNacionalidade
            final EmailAddress email = person.getEmailAddressForSendingEmails();
            row.setCell(10, email == null ? "email not found" : email.getPresentationValue()); //Email      

            InstitutionCodeProvider institutionCodeProvider = RenatesIntegrationConfiguration.getInstitutionCodeProvider();
            row.setCell(12, institutionCodeProvider.getEstablishmentCode()); //CodEstabelecimentoPortugues
            final DegreeCurricularPlan degreeCurricularPlan = thesis.getEnrolment().getDegreeCurricularPlanOfStudent();

            row.setCell(13, institutionCodeProvider.getOrganicUnitCode(degreeCurricularPlan)); //codUnidadeOrganica
            final Degree degree = degreeCurricularPlan.getDegree();

            row.setCell(14, degree.getMinistryCode()); //CodCurso
            row.setCell(15, degree.getPresentationName()); //Curso

            row.setCell(16, "1000017"); //CodEspecializacao
            row.setCell(17, "Sem especialização"); //Especializacao
            row.setCell(18, thesis.getTitle().getContent(PT)); //TituloTrabalho

            final Person orientator1 = getOrientor(thesis);

            if (orientator1 != null) {
                row.setCell(19, orientator1.getName()); //NomeOrientador1
                row.setCell(20, orientator1.getDocumentIdNumber()); //NumIdentificacao_Orientador1                
                final String orienter1IdType = idDocumentTypeToNumber(orientator1.getIdDocumentType());
                row.setCell(21, orienter1IdType); //TipoIdentificacao_Orientador1
                row.setCell(22, orienter1IdType.equals("9") ? orientator1.getIdDocumentType().getLocalizedName(PT) : ""); //OutroTipoIdentificacao_Orientador1

            }

            row.setCell(45, thesis.getKeywordsPt()); //Palavras_chave
            row.setCell(46, "Não"); //Curso_Parceria           
            row.setCell(56, conclusionProcess.getConclusionDate().toString("dd-MM-yyyy")); //DataGrauEmPortugal
            row.setCell(57, conclusionProcess.getFinalGrade().getValue()); //ClassificacaoID

        }

        return spreadsheet;
    }

    private Enrolment getThesisEnrolment(CurriculumGroup group) {
        Predicate<CurriculumLine> isConcludedEnrolment = e -> (e.isEnrolment() && ((Enrolment) e).isDissertation()
                && e.isApproved() && ((Enrolment) e).getPossibleThesis() != null
                && ((Enrolment) e).getPossibleThesis().isFinalAndApprovedThesis());

        Set<CurriculumLine> curriculumLines = group.getAllCurriculumLines().stream()
                .filter(e -> (e.isDismissal() && ((Dismissal) e).getCurricularCourse() != null
                        && ((Dismissal) e).getCurricularCourse().isDissertation()
                        && ((Dismissal) e).getCredits().isSubstitution()) || isConcludedEnrolment.test(e))
                .collect(Collectors.toSet());

        if (curriculumLines.isEmpty()) {
            return null;
        }

        for (CurriculumLine curriculumLine : curriculumLines) {
            if (curriculumLine instanceof Dismissal) {
                Dismissal dismissal = (Dismissal) curriculumLine;
                Substitution substitution = (Substitution) dismissal.getCredits();
                Enrolment enrolment =
                        (Enrolment) substitution.getEnrolmentsSet().stream().filter(e -> e.getIEnrolment().isEnrolment())
                                .filter(e -> isConcludedEnrolment.test((CurriculumLine) e.getIEnrolment()))
                                .map(e -> e.getIEnrolment()).findAny().orElse(null);
                if (enrolment != null) {
                    return enrolment;
                }
            }
            if (curriculumLine instanceof Enrolment) {
                return (Enrolment) curriculumLine;
            }
        }
        return null;
    }

    private String idDocumentTypeToNumber(IDDocumentType idDocumentType) {

        switch (idDocumentType) {
        case IDENTITY_CARD:
            return "1";

        case CITIZEN_CARD:
            return "1";

        case PASSPORT:
            return "2";

        case RESIDENCE_AUTHORIZATION:
            return "3";

        case FOREIGNER_IDENTITY_CARD:
            return "4";

        case OTHER:
            return "9";

        case NATIVE_COUNTRY_IDENTITY_CARD:
            return "9";

        case NAVY_IDENTITY_CARD:
            return "9";

        case AIR_FORCE_IDENTITY_CARD:
            return "9";

        case MILITARY_IDENTITY_CARD:
            return "9";

        case EXTERNAL:
            return "9";

        default:
            return "9";
        }
    }

    private Person getOrientor(Thesis thesis) {
        for (ThesisEvaluationParticipant thesisEvaluationParticipant : thesis
                .getAllParticipants(ThesisParticipationType.ORIENTATOR)) {
            Person orientator = thesisEvaluationParticipant.getPerson();
            if (orientator != null) {
                return orientator;
            }
        }

        return null;
    }

}
