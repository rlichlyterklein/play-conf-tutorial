package models;

import javax.persistence.*;

import play.data.validation.Constraints;
import play.db.ebean.Model;
/**
 * Created by lichlyterklein on 12/23/13.
 */
@Entity
public class Speaker extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String name;

    @Constraints.Required
    @Constraints.Email
    public String email;

    @Constraints.Required
    @Constraints.MinLength(value=10)
    @Constraints.MaxLength(value=1000)
    @Column(length = 1000)
    public String bio;

    @Constraints.Required
    public String twitterId;

    @Constraints.Required
    public String pictureUrl;

}
