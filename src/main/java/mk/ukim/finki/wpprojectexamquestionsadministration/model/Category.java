package mk.ukim.finki.wpprojectexamquestionsadministration.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.enumerations.FormatType;
import mk.ukim.finki.wpprojectexamquestionsadministration.model.questions.BaseQuestion;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String info;

    @Enumerated(EnumType.STRING)
    private FormatType infoTextFormat = FormatType.HTML;

    private String idNumber;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "parent_id")
    private Category parentCategory;
    @Transient
    private Long parentId;

    @JsonIgnore
    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Category> subcategories = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<BaseQuestion> questions;

    public Category(String name) {
        this.name = name;
        this.questions = new ArrayList<>();
    }
    public Category(String name, List<BaseQuestion> questions) {
        this.name = name;
        this.questions = questions;
    }
    public Category(String name, Category parentCategory) {
        this.name = name;
        this.parentCategory = parentCategory;
    }

    public Category(String name, String info, FormatType infoTextFormat, String idNumber) {
        this.name = name;
        this.info = info;
        this.infoTextFormat = infoTextFormat;
        this.idNumber = idNumber;
        this.questions = new ArrayList<>();
    }

    public Category(String name, String info, FormatType infoTextFormat, String idNumber, Category parentCategory) {
        this.name = name;
        this.info = info;
        this.infoTextFormat = infoTextFormat;
        this.idNumber = idNumber;
        this.parentCategory = parentCategory;
        this.questions = new ArrayList<>();
    }
    public void addSubcategory(Category subcategory) {
        subcategories.add(subcategory);
        subcategory.setParentCategory(this);
    }

    public void removeSubcategory(Category subcategory) {
        subcategories.remove(subcategory);
        subcategory.setParentCategory(null);
    }
    public Long getParentId() {
        return parentCategory != null ? parentCategory.getId() : null;
    }
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", info='" + info + '\'' +
                ", infoTextFormat=" + infoTextFormat +
                ", idNumber='" + idNumber + '\'' +
                ", parentId=" + (parentCategory != null ? parentCategory.getId() : "null") +
                // Avoid calling toString on potentially lazy-loaded objects
                ", subcategoriesCount=" + (subcategories != null ? subcategories.size() : "Not loaded") +
                '}';
    }
}
