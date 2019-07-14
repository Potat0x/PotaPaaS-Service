package pl.potat0x.potapaas.potapaasservice.app;

import pl.potat0x.potapaas.potapaasservice.core.AppType;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.Objects;

@Entity
public class App {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "app_instance_id", referencedColumnName = "id")
    private AppInstance appInstance;

    @Enumerated(value = EnumType.STRING)
    private AppType type;

    private String uuid;

    private String name;
    private String sourceRepoUrl;
    private String sourceBranchName;

    public App(String uuid, AppInstance appInstance, String name, AppType type, String sourceRepoUrl, String sourceBranchName) {
        this.appInstance = appInstance;
        this.name = name;
        this.type = type;
        this.sourceRepoUrl = sourceRepoUrl;
        this.sourceBranchName = sourceBranchName;
        this.uuid = uuid;
    }

    protected App() {
    }

    public String getUuid() {
        return uuid;
    }

    public Long getId() {
        return id;
    }

    public AppInstance getAppInstance() {
        return appInstance;
    }

    public AppType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSourceRepoUrl() {
        return sourceRepoUrl;
    }

    public String getSourceBranchName() {
        return sourceBranchName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        App app = (App) o;
        return uuid.equals(app.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
