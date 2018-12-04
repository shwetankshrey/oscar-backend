package com.oscar;

import com.clarkparsia.owlapi.explanation.BlackBoxExplanation;
import com.clarkparsia.owlapi.explanation.HSTExplanationGenerator;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.stream.Stream;

public class Processor {

    final String baseIRI = "http://www.semanticweb.org/aakashtanwar/ontologies/2018/11/movies.owl";
    OWLOntology _ont;
    OWLOntologyManager _man;
    OWLReasoner _rsn1;
    OWLReasoner _rsn2;
    OWLReasonerFactory _rsf;
    OWLDataFactory _daf;
    private String response = "";

    public Processor() {
        File f = new File("src/main/resources/Movie.owl");
        _man = OWLManager.createOWLOntologyManager();
        try {
            _ont = _man.loadOntologyFromOntologyDocument(f);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        _rsn1 = new Reasoner(new Configuration(), _ont);
        Configuration _cfg = new Configuration();
        _cfg.throwInconsistentOntologyException = false;
        _rsf = new ReasonerFactory() {
            protected OWLReasoner createHermiTOWLReasoner(Configuration configuration, OWLOntology ontology) {
                configuration.throwInconsistentOntologyException = false;
                return new Reasoner(configuration, ontology);
            }
        };
        _rsn2 = _rsf.createReasoner(_ont, _cfg);
        _daf = _man.getOWLDataFactory();
    }

    public String process(String query) {
        if (query.startsWith("!axiom")) {
            return addAxiom(query.substring(7));
        }
        if (query.startsWith("!check")) {
            return checkConsistency();
        }
        if (subClassQuery(query)) return response;
        if (instanceQuery(query)) return response;
        if (typeQuery(query)) return response;
        if (subPropertyQuery(query)) return response;
        if (characteristicQuery(query)) return response;
        return "sOrI fOr My BaD eNgLiS ! 011010010 :(";
    }

    private String addAxiom(String query) {
        query = query.replace(")", "");
        OWLAxiom a = null;
        String[] tok = query.split("\\(");
        if (tok[0].equals("Declaration")) {
            if (tok[1].equals("Class")) {
                OWLClass c = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok[2]));
                a = _daf.getOWLDeclarationAxiom(c);
                _ont.addAxiom(a);
            }
            if (tok[1].equals("NamedIndividual")) {
                OWLNamedIndividual i = _daf.getOWLNamedIndividual(IRI.create(baseIRI+"#"+tok[2]));
                a = _daf.getOWLDeclarationAxiom(i);
                _ont.addAxiom(a);
            }
        }
        if (tok[0].equals("ClassAssertion")) {
            String[] tok2 = tok[1].split(" ");
            OWLClass c = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok2[0]));
            OWLNamedIndividual i = _daf.getOWLNamedIndividual(IRI.create(baseIRI+"#"+tok2[1]));
            a = _daf.getOWLClassAssertionAxiom(c, i);
            _ont.addAxiom(a);
        }
        if (tok[0].equals("ObjectPropertyAssertion")) {
            String[] tok2 = tok[1].split(" ");
            OWLObjectProperty p = _daf.getOWLObjectProperty(IRI.create(baseIRI+"#"+tok2[0]));
            OWLNamedIndividual i1 = _daf.getOWLNamedIndividual(IRI.create(baseIRI+"#"+tok2[1]));
            OWLNamedIndividual i2 = _daf.getOWLNamedIndividual(IRI.create(baseIRI+"#"+tok2[2]));
            a = _daf.getOWLObjectPropertyAssertionAxiom(p, i1, i2);
            _ont.addAxiom(a);
        }
        if (tok[0].equals("DataPropertyAssertion")) {
            String[] tok2 = tok[1].split(" ");
            OWLDataPropertyExpression p = _daf.getOWLDataProperty(IRI.create(baseIRI+"#"+tok2[0]));
            OWLNamedIndividual i1 = _daf.getOWLNamedIndividual(IRI.create(baseIRI+"#"+tok2[1]));
            a = _daf.getOWLDataPropertyAssertionAxiom(p, i1, tok2[2]);
            _ont.addAxiom(a);
        }
        if (tok[0].equals("SubClassOf")) {
            String[] tok2 = tok[1].split(" ");
            OWLClass c1 = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok2[0]));
            OWLClass c2 = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok2[1]));
            a = _daf.getOWLSubClassOfAxiom(c1, c2);
            _ont.addAxiom(a);
        }
        if (tok[0].equals("EquivalentClasses")) {
            String[] tok2 = tok[1].split(" ");
            OWLClass c1 = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok2[0]));
            OWLClass c2 = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok2[1]));
            a = _daf.getOWLEquivalentClassesAxiom(c1, c2);
            _ont.addAxiom(a);
        }
        if (tok[0].equals("DisjointClasses")) {
            String[] tok2 = tok[1].split(" ");
            OWLClass c1 = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok2[0]));
            OWLClass c2 = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok2[1]));
            a = _daf.getOWLDisjointClassesAxiom(c1, c2);
            _ont.addAxiom(a);
        }
        if (tok[0].equals("SubPropertyOf")) {
            String[] tok2 = tok[1].split(" ");
            if (!_ont.containsDataPropertyInSignature(IRI.create(baseIRI + "#" + tok2[0]))) {
                OWLObjectProperty p1 = _daf.getOWLObjectProperty(IRI.create(baseIRI + "#" + tok2[0]));
                OWLObjectProperty p2 = _daf.getOWLObjectProperty(IRI.create(baseIRI + "#" + tok2[1]));
                a = _daf.getOWLSubObjectPropertyOfAxiom(p1, p2);
                _ont.addAxiom(a);
            }
            else {
                OWLDataProperty p1 = _daf.getOWLDataProperty(IRI.create(baseIRI + "#" + tok2[0]));
                OWLDataProperty p2 = _daf.getOWLDataProperty(IRI.create(baseIRI + "#" + tok2[1]));
                a = _daf.getOWLSubDataPropertyOfAxiom(p1, p2);
                _ont.addAxiom(a);
            }
        }
        if (tok[0].equals("DisjointProperties")) {
            String[] tok2 = tok[1].split(" ");
            if (!_ont.containsDataPropertyInSignature(IRI.create(baseIRI + "#" + tok2[0]))) {
                OWLObjectProperty p1 = _daf.getOWLObjectProperty(IRI.create(baseIRI + "#" + tok2[0]));
                OWLObjectProperty p2 = _daf.getOWLObjectProperty(IRI.create(baseIRI + "#" + tok2[1]));
                a = _daf.getOWLDisjointObjectPropertiesAxiom(p1, p2);
                _ont.addAxiom(a);
            }
            else {
                OWLDataProperty p1 = _daf.getOWLDataProperty(IRI.create(baseIRI + "#" + tok2[0]));
                OWLDataProperty p2 = _daf.getOWLDataProperty(IRI.create(baseIRI + "#" + tok2[1]));
                a = _daf.getOWLDisjointDataPropertiesAxiom(p1, p2);
                _ont.addAxiom(a);
            }
        }
        if (tok[0].equals("ObjectPropertyDomain")) {
            String[] tok2 = tok[1].split(" ");
            OWLObjectProperty p1 = _daf.getOWLObjectProperty(IRI.create(baseIRI + "#" + tok2[0]));
            OWLClass c2 = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok2[1]));
            a = _daf.getOWLObjectPropertyDomainAxiom(p1, c2);
            _ont.addAxiom(a);
        }
        if (tok[0].equals("ObjectPropertyRange")) {
            String[] tok2 = tok[1].split(" ");
            OWLObjectProperty p1 = _daf.getOWLObjectProperty(IRI.create(baseIRI + "#" + tok2[0]));
            OWLClass c2 = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok2[1]));
            a = _daf.getOWLObjectPropertyRangeAxiom(p1, c2);
            _ont.addAxiom(a);
        }
        if (tok[0].equals("DataPropertyDomain")) {
            String[] tok2 = tok[1].split(" ");
            OWLDataProperty p1 = _daf.getOWLDataProperty(IRI.create(baseIRI + "#" + tok2[0]));
            OWLClass c2 = _daf.getOWLClass(IRI.create(baseIRI+"#"+tok2[1]));
            a = _daf.getOWLDataPropertyDomainAxiom(p1, c2);
            _ont.addAxiom(a);
        }
        if (a == null) {
            return "This axiom is not yet supported. :(";
        }
        else {
            String s = checkConsistency();
            if (s.equals("The knowledge base is consistent. :)")) {
                try {
                    File f = new File("src/main/resources/Movie.owl");
                    _man.saveOntology(_ont, new FunctionalSyntaxDocumentFormat(), new FileOutputStream(f));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (OWLOntologyStorageException e) {
                    e.printStackTrace();
                }
                return ("The axiom was added. " + s);
            }
            else {
                _ont.removeAxiom(a);
                return ("We cannot add this axiom due to inconsistency. " + s);
            }
        }
    }

    private String checkConsistency() {
        if (_rsn2.isConsistent()) return "The knowledge base is consistent. :)";
        else return inconsistencyExplanation();
    }

    private String inconsistencyExplanation() {
        BlackBoxExplanation exp = new BlackBoxExplanation(_ont, _rsf, _rsn2);
        HSTExplanationGenerator gen = new HSTExplanationGenerator(exp);
        OWLClass ac = _daf.getOWLClass(IRI.create(baseIRI + "#Actor"));
        Set<OWLAxiom> _ax = gen.getExplanation(ac);
        String str = "The knowledge base is inconsistent. :0";
        for (OWLAxiom a : _ax) {
            str = str + "\n" + a;
        }
        return str;
    }

    private boolean subClassQuery(String query) {
        if (query.contains("SubClassOf")) {
            String[] tok = query.split(" ");
            if (!_ont.containsClassInSignature(IRI.create(baseIRI + "#" + tok[0]))) {
                response = tok[0] + " is not a class.";
                return true;
            }
            if (!_ont.containsClassInSignature(IRI.create(baseIRI + "#" + tok[2]))) {
                response = tok[2] + " is not a class.";
                return true;
            }
            OWLClass c1 = _daf.getOWLClass(baseIRI + "#" + tok[0]);
            OWLClass c2 = _daf.getOWLClass(baseIRI + "#" + tok[2]);
            Stream<OWLClass> subclasses = _rsn1.getSubClasses(c2).entities();
            for (OWLClass cl : (Iterable<OWLClass>) () -> subclasses.iterator()) {
                if (c1.equals(cl)) {
                    response = "Yes! " + tok[0] + " is a subclass of " + tok[2] + ".";
                    return true;
                }
            }
            response = "No! " + tok[0] + " is not a subclass of " + tok[2] + ".";
            return true;
        }
        else {
            // Is A a subclass of B?
            if (query.endsWith("?")) query = query.substring(0, query.length() - 1);
            if (query.contains("subclass")) {
                String[] tok = query.split(" ");
                if (!_ont.containsClassInSignature(IRI.create(baseIRI + "#" + tok[1]))) {
                    response = tok[1] + " is not a class.";
                    return true;
                }
                if (tok[5].equals("itself")) {
                    response = "Every class is a subclass of itself.";
                    return true;
                }
                if (!_ont.containsClassInSignature(IRI.create(baseIRI + "#" + tok[5]))) {
                    response = tok[5] + " is not a class.";
                    return true;
                }
                OWLClass c1 = _daf.getOWLClass(baseIRI + "#" + tok[1]);
                OWLClass c2 = _daf.getOWLClass(baseIRI + "#" + tok[5]);
                Stream<OWLClass> subclasses = _rsn1.getSubClasses(c2).entities();
                for (OWLClass cl : (Iterable<OWLClass>) () -> subclasses.iterator()) {
                    if (c1.equals(cl)) {
                        response = "Yes! " + tok[1] + " is a subclass of " + tok[5] + ".";
                        return true;
                    }
                }
                response = "No! " + tok[1] + " is not a subclass of " + tok[5] + ".";
                return true;
            }
        }
        return false;
    }

    private boolean instanceQuery(String query) {
        if (query.contains("InstanceOf")) {
            String[] tok = query.split(" ");
            if (!_ont.containsIndividualInSignature(IRI.create(baseIRI + "#" + tok[0]))) {
                response = tok[0] + " is not an individual.";
                return true;
            }
            if (!_ont.containsClassInSignature(IRI.create(baseIRI + "#" + tok[2]))) {
                response = tok[2] + " is not a class.";
                return true;
            }
            OWLNamedIndividual i1 = _daf.getOWLNamedIndividual(baseIRI + "#" + tok[0]);
            OWLClass c2 = _daf.getOWLClass(baseIRI + "#" + tok[2]);
            Stream<OWLNamedIndividual> instances = _rsn1.getInstances(c2).entities();
            for (OWLNamedIndividual in : (Iterable<OWLNamedIndividual>) () -> instances.iterator()) {
                if (i1.equals(in)) {
                    response = "Yes! " + tok[0] + " is an instance of " + tok[2] + ".";
                    return true;
                }
            }
            response = "No! " + tok[0] + " is not an instance of " + tok[2] + ".";
            return true;
        }
        else {
            // Is A (a|an| ... of) B?
            if (query.endsWith("?")) query = query.substring(0, query.length() - 1);
            if (query.contains("Is") && (query.contains("a") || query.contains("an")) && query.split(" ").length == 4) {
                String[] tok = query.split(" ");
                int ind1 = 1;
                int ind2 = 3;
                if (!_ont.containsIndividualInSignature(IRI.create(baseIRI + "#" + tok[1]))) {
                    response = tok[1] + " is not an individual.";
                    return true;
                }
                if (!_ont.containsClassInSignature(IRI.create(baseIRI + "#" + tok[3]))) {
                    response = tok[3] + " is not a class.";
                    return true;
                }
                OWLNamedIndividual i1 = _daf.getOWLNamedIndividual(baseIRI + "#" + tok[1]);
                OWLClass c2 = _daf.getOWLClass(baseIRI + "#" + tok[3]);
                Stream<OWLNamedIndividual> instances = _rsn1.getInstances(c2).entities();
                for (OWLNamedIndividual in : (Iterable<OWLNamedIndividual>) () -> instances.iterator()) {
                    if (i1.equals(in)) {
                        response = "Yes! " + tok[1] + " is an instance of " + tok[3] + ".";
                        return true;
                    }
                }
                response = "No! " + tok[1] + " is not an instance of " + tok[3] + ".";
                return true;
            }
        }
        return false;
    }

    private boolean typeQuery(String query) {
        if (query.contains("GetTypes")) {
            String[] tok = query.split(" ");
            if (!_ont.containsIndividualInSignature(IRI.create(baseIRI + "#" + tok[1]))) {
                response = tok[1] + " is not an individual.";
                return true;
            }
            OWLNamedIndividual i1 = _daf.getOWLNamedIndividual(baseIRI + "#" + tok[1]);
            Stream<OWLClass> types = _rsn1.getTypes(i1).entities();
            response = "";
            for (OWLClass in : (Iterable<OWLClass>) () -> types.iterator()) {
                response = response + in.getIRI().getShortForm() + ", ";
            }
            response = "Types for " + tok[1] + " are " + response.substring(0, response.length() - 2) + ".";
            return true;
        }
        else {
            // ... types of A ...
            if (query.endsWith("?")) query = query.substring(0, query.length() - 1);
            if (query.contains("types")) {
                String[] tok = query.split(" ");
                int ind = 5;
                for (int i = 0 ; i < tok.length ; i++) {
                    if (tok[i].equals("of")) {
                        ind = i + 1;
                    }
                }
                if (!_ont.containsIndividualInSignature(IRI.create(baseIRI + "#" + tok[ind]))) {
                    response = tok[ind] + " is not an individual.";
                    return true;
                }
                OWLNamedIndividual i1 = _daf.getOWLNamedIndividual(baseIRI + "#" + tok[ind]);
                Stream<OWLClass> types = _rsn1.getTypes(i1).entities();
                response = "";
                for (OWLClass in : (Iterable<OWLClass>) () -> types.iterator()) {
                    response = response + in.getIRI().getShortForm() + ", ";
                }
                response = "Types for " + tok[ind] + " are " + response.substring(0, response.length() - 2) + ".";
                return true;
            }
        }
        return false;
    }

    private boolean subPropertyQuery(String query) {
        if (query.contains("SubPropertyOf")) {
            String[] tok = query.split(" ");
            if (!_ont.containsObjectPropertyInSignature(IRI.create(baseIRI + "#" + tok[0]))) {
                if (!_ont.containsDataPropertyInSignature(IRI.create(baseIRI + "#" + tok[0]))) {
                    response = tok[0] + " is not a valid property.";
                    return true;
                }
                if (!_ont.containsDataPropertyInSignature(IRI.create(baseIRI + "#" + tok[2]))) {
                    response = tok[2] + " is not a valid property.";
                    return true;
                }
                OWLDataProperty p1 = _daf.getOWLDataProperty(baseIRI + "#" + tok[0]);
                OWLDataProperty p2 = _daf.getOWLDataProperty(baseIRI + "#" + tok[2]);
                Stream<OWLDataProperty> subproperties = _rsn1.getSubDataProperties(p2).entities();
                for (OWLDataProperty pr : (Iterable<OWLDataProperty>) () -> subproperties.iterator()) {
                    if (p1.equals(pr)) {
                        response = "Yes! " + tok[0] + " is a subproperty of " + tok[2] + ".";
                        return true;
                    }
                }
                response = "No! " + tok[0] + " is not a subproperty of " + tok[2] + ".";
                return true;
            } else {
                if (!_ont.containsObjectPropertyInSignature(IRI.create(baseIRI + "#" + tok[0]))) {
                    response = tok[0] + " is not a valid property.";
                    return true;
                }
                if (!_ont.containsObjectPropertyInSignature(IRI.create(baseIRI + "#" + tok[2]))) {
                    response = tok[2] + " is not a valid property.";
                    return true;
                }
                OWLObjectProperty p1 = _daf.getOWLObjectProperty(baseIRI + "#" + tok[0]);
                OWLObjectProperty p2 = _daf.getOWLObjectProperty(baseIRI + "#" + tok[2]);
                Stream<OWLObjectPropertyExpression> subproperties = _rsn1.getSubObjectProperties(p2).entities();
                for (OWLObjectPropertyExpression pr : (Iterable<OWLObjectPropertyExpression>) () -> subproperties.iterator()) {
                    if (p1.equals(pr)) {
                        response = "Yes! " + tok[0] + " is a subproperty of " + tok[2] + ".";
                        return true;
                    }
                }
                response = "No! " + tok[0] + " is not a subproperty of " + tok[2] + ".";
                return true;
            }
        }
        else {
            // .... A (is|a) subproperty of B ....
            if (query.endsWith("?")) query = query.substring(0, query.length() - 1);
            if (query.contains("subproperty")) {
                String[] tok = query.split(" ");
                int ind1 = 1;
                int ind2 = 5;
                for (int i = 0 ; i < tok.length ; i++) {
                    if (tok[i].equals("subproperty")) {
                        if (tok[i-1].equals("a") || tok[i-1].equals("is")) {
                            ind1 = i - 2;
                        }
                        else {
                            ind1 = i - 1;
                        }

                        ind2 = i + 2;
                    }
                }
                if (!_ont.containsObjectPropertyInSignature(IRI.create(baseIRI + "#" + tok[ind1]))) {
                    if (!_ont.containsDataPropertyInSignature(IRI.create(baseIRI + "#" + tok[ind1]))) {
                        response = tok[ind1] + " is not a valid property.";
                        return true;
                    }
                    if (!_ont.containsDataPropertyInSignature(IRI.create(baseIRI + "#" + tok[ind2]))) {
                        response = tok[ind2] + " is not a valid property.";
                        return true;
                    }
                    OWLDataProperty p1 = _daf.getOWLDataProperty(baseIRI + "#" + tok[ind1]);
                    OWLDataProperty p2 = _daf.getOWLDataProperty(baseIRI + "#" + tok[ind2]);
                    Stream<OWLDataProperty> subproperties = _rsn1.getSubDataProperties(p2).entities();
                    for (OWLDataProperty pr : (Iterable<OWLDataProperty>) () -> subproperties.iterator()) {
                        if (p1.equals(pr)) {
                            response = "Yes! " + tok[ind1] + " is a subproperty of " + tok[ind2] + ".";
                            return true;
                        }
                    }
                    response = "No! " + tok[ind1] + " is not a subproperty of " + tok[ind2] + ".";
                    return true;
                } else {
                    if (!_ont.containsObjectPropertyInSignature(IRI.create(baseIRI + "#" + tok[ind1]))) {
                        response = tok[ind1] + " is not a valid property.";
                        return true;
                    }
                    if (!_ont.containsObjectPropertyInSignature(IRI.create(baseIRI + "#" + tok[ind2]))) {
                        response = tok[ind2] + " is not a valid property.";
                        return true;
                    }
                    OWLObjectProperty p1 = _daf.getOWLObjectProperty(baseIRI + "#" + tok[ind1]);
                    OWLObjectProperty p2 = _daf.getOWLObjectProperty(baseIRI + "#" + tok[ind2]);
                    Stream<OWLObjectPropertyExpression> subproperties = _rsn1.getSubObjectProperties(p2).entities();
                    for (OWLObjectPropertyExpression pr : (Iterable<OWLObjectPropertyExpression>) () -> subproperties.iterator()) {
                        if (p1.equals(pr)) {
                            response = "Yes! " + tok[ind1] + " is a subproperty of " + tok[ind2] + ".";
                            return true;
                        }
                    }
                    response = "No! " + tok[ind1] + " is not a subproperty of " + tok[ind2] + ".";
                    return true;
                }
            }
        }
        return false;
    }

    private boolean characteristicQuery(String query) {
        if (query.contains("CharacteristicsOf")) {
            String[] tok = query.split(" ");
            if (!_ont.containsObjectPropertyInSignature(IRI.create(baseIRI + "#" + tok[1])) &&
                    !_ont.containsDataPropertyInSignature(IRI.create(baseIRI + "#" + tok[1]))) {
                response = tok[1] + " is not a property.";
                return true;
            }
            if (_ont.containsObjectPropertyInSignature(IRI.create(baseIRI + "#" + tok[1]))) {
                OWLObjectProperty p1 = _daf.getOWLObjectProperty(baseIRI + "#" + tok[1]);
                NodeSet<OWLClass> doms = _rsn1.getObjectPropertyDomains(p1);
                response = "Domains for " + tok[1] + " are ";
                for (Node<OWLClass> _dom : doms) {
                    response = response + _dom.getRepresentativeElement().getIRI().getShortForm() + ", ";
                }
                response = response.substring(0, response.length() - 2) + ". ";
                NodeSet<OWLClass> rans = _rsn1.getObjectPropertyRanges(p1);
                response = response + "Ranges for " + tok[1] + " are ";
                for (Node<OWLClass> _ran : rans) {
                    response = response + _ran.getRepresentativeElement().getIRI().getShortForm() + ", ";
                }
                response = response.substring(0, response.length() - 2) + ". ";
                response = response + "The subproperties of " + tok[1] + " are ";
                Stream<OWLObjectPropertyExpression> subproperties = _rsn1.getSubObjectProperties(p1).entities();
                for (OWLObjectPropertyExpression pr : (Iterable<OWLObjectPropertyExpression>) () -> subproperties.iterator()) {
                    response = response + ((OWLObjectProperty)pr).getIRI().getShortForm() + ", ";
                }
                response = response.substring(0, response.length() - 2) + ". ";
                response = response + tok[1] + " is a subproperty of ";
                Stream<OWLObjectPropertyExpression> superproperties = _rsn1.getSuperObjectProperties(p1).entities();
                for (OWLObjectPropertyExpression pr : (Iterable<OWLObjectPropertyExpression>) () -> superproperties.iterator()) {
                    response = response + ((OWLObjectProperty)pr).getIRI().getShortForm() + ", ";
                }
                response = response.substring(0, response.length() - 2) + ". ";
                return true;
            } else {
                OWLDataProperty p1 = _daf.getOWLDataProperty(baseIRI + "#" + tok[1]);
                NodeSet<OWLClass> doms = _rsn1.getDataPropertyDomains(p1);
                response = "Domains for " + tok[1] + " are ";
                for (Node<OWLClass> _dom : doms) {
                    response = response + _dom.getRepresentativeElement().getIRI().getShortForm() + ", ";
                }
                response = response.substring(0, response.length() - 2) + ". ";
                response = response + tok[1] + " is a data property and has no ranges.";
                response = response + "The subproperties of " + tok[1] + " are ";
                Stream<OWLDataProperty> subproperties = _rsn1.getSubDataProperties(p1).entities();
                for (OWLDataProperty pr : (Iterable<OWLDataProperty>) () -> subproperties.iterator()) {
                    response = response + pr.getIRI().getShortForm() + ", ";
                }
                response = response.substring(0, response.length() - 2) + ". ";
                response = response + tok[1] + " is a subproperty of ";
                Stream<OWLDataProperty> superproperties = _rsn1.getSuperDataProperties(p1).entities();
                for (OWLDataProperty pr : (Iterable<OWLDataProperty>) () -> superproperties.iterator()) {
                    response = response + pr.getIRI().getShortForm() + ", ";
                }
                response = response.substring(0, response.length() - 2) + ". ";
                return true;
            }
        } else {
            // .... characteristics of A ....
            if (query.endsWith("?")) query = query.substring(0, query.length() - 1);
            if (query.contains("characteristics")) {
                String[] tok = query.split(" ");
                int ind = 5;
                for (int i = 0  ; i < tok.length ; i++) {
                    if (tok[i].equals("of")) {
                        ind = i + 1;
                        break;
                    }
                }
                if (!_ont.containsObjectPropertyInSignature(IRI.create(baseIRI + "#" + tok[ind])) &&
                        !_ont.containsDataPropertyInSignature(IRI.create(baseIRI + "#" + tok[ind]))) {
                    response = tok[ind] + " is not a property.";
                    return true;
                }
                if (_ont.containsObjectPropertyInSignature(IRI.create(baseIRI + "#" + tok[ind]))) {
                    OWLObjectProperty p1 = _daf.getOWLObjectProperty(baseIRI + "#" + tok[ind]);
                    NodeSet<OWLClass> doms = _rsn1.getObjectPropertyDomains(p1);
                    response = "Domains for " + tok[ind] + " are ";
                    for (Node<OWLClass> _dom : doms) {
                        response = response + _dom.getRepresentativeElement().getIRI().getShortForm() + ", ";
                    }
                    response = response.substring(0, response.length() - 2) + ". ";
                    NodeSet<OWLClass> rans = _rsn1.getObjectPropertyRanges(p1);
                    response = response + "Ranges for " + tok[ind] + " are ";
                    for (Node<OWLClass> _ran : rans) {
                        response = response + _ran.getRepresentativeElement().getIRI().getShortForm() + ", ";
                    }
                    response = response.substring(0, response.length() - 2) + ". ";
                    response = response + "The subproperties of " + tok[ind] + " are ";
                    Stream<OWLObjectPropertyExpression> subproperties = _rsn1.getSubObjectProperties(p1).entities();
                    for (OWLObjectPropertyExpression pr : (Iterable<OWLObjectPropertyExpression>) () -> subproperties.iterator()) {
                        response = response + ((OWLObjectProperty)pr).getIRI().getShortForm() + ", ";
                    }
                    response = response.substring(0, response.length() - 2) + ". ";
                    response = response + tok[ind] + " is a subproperty of ";
                    Stream<OWLObjectPropertyExpression> superproperties = _rsn1.getSuperObjectProperties(p1).entities();
                    for (OWLObjectPropertyExpression pr : (Iterable<OWLObjectPropertyExpression>) () -> superproperties.iterator()) {
                        response = response + ((OWLObjectProperty)pr).getIRI().getShortForm() + ", ";
                    }
                    response = response.substring(0, response.length() - 2) + ". ";
                    return true;
                } else {
                    OWLDataProperty p1 = _daf.getOWLDataProperty(baseIRI + "#" + tok[ind]);
                    NodeSet<OWLClass> doms = _rsn1.getDataPropertyDomains(p1);
                    response = "Domains for " + tok[ind] + " are ";
                    for (Node<OWLClass> _dom : doms) {
                        response = response + _dom.getRepresentativeElement().getIRI().getShortForm() + ", ";
                    }
                    response = response.substring(0, response.length() - 2) + ". ";
                    response = response + tok[ind] + " is a data property and has no ranges.";
                    response = response + "The subproperties of " + tok[ind] + " are ";
                    Stream<OWLDataProperty> subproperties = _rsn1.getSubDataProperties(p1).entities();
                    for (OWLDataProperty pr : (Iterable<OWLDataProperty>) () -> subproperties.iterator()) {
                        response = response + pr.getIRI().getShortForm() + ", ";
                    }
                    response = response.substring(0, response.length() - 2) + ". ";
                    response = response + tok[ind] + " is a subproperty of ";
                    Stream<OWLDataProperty> superproperties = _rsn1.getSuperDataProperties(p1).entities();
                    for (OWLDataProperty pr : (Iterable<OWLDataProperty>) () -> superproperties.iterator()) {
                        response = response + pr.getIRI().getShortForm() + ", ";
                    }
                    response = response.substring(0, response.length() - 2) + ". ";
                    return true;
                }
            }
        }
        return false;
    }
}
