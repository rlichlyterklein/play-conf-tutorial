package models;

import play.Logger;
import play.data.validation.Constraints;
import play.db.ebean.Model;
import play.libs.Akka;
import play.libs.F;
import play.libs.F.Promise;
import scala.concurrent.ExecutionContext;

import javax.persistence.*;
import javax.validation.Valid;

/**
 * Created by lichlyterklein on 12/23/13.
 */

@Entity
public class Proposal extends Model {

    @Id
    public Long id;

    @Constraints.Required
    public String title;

    @Constraints.Required
    @Constraints.MinLength(value=10)
    @Constraints.MaxLength(value=1000)
    @Column(length = 1000)
    public String proposal;

    @Constraints.Required
    public SessionType type = SessionType.OneHourTalk;

    @Constraints.Required
    public boolean isApproved = false;

    public String keywords;

    @Valid

    @OneToOne(cascade = CascadeType.ALL)
    public Speaker speaker;

    private static Finder<Long, Proposal> find = new Finder<>(Long.class,Proposal.class);

    private static ExecutionContext ctx = Akka.system().dispatchers().lookup("akka.db-dispatcher");

    public static F.Promise<Proposal> findKeynote() {

        return F.Promise.promise(new F.Function0<Proposal>() {
            @Override
            public Proposal apply() throws Throwable {
                return find.where().eq("type",SessionType.Keynote).findUnique();
            }
        }, ctx).recover(new F.Function<Throwable, Proposal>() {
            @Override
            public Proposal apply(Throwable throwable) throws Throwable {
                Logger.error("failed to get keynote");
                Proposal p = new Proposal();
                p.title = "COMING SOON";
                p.proposal = "";
                Speaker speaker = new Speaker();
                speaker.name = "";
                speaker.pictureUrl = "";
                speaker.twitterId = "";
                p.speaker = speaker;
                return p;
            }
        },ctx);
    }

    public F.Promise<Void> asyncSave(){
        return F.Promise.promise(new F.Function0<Void>(){
            @Override
            public Void apply() throws Throwable {
                save();
                return null;
            }
        }, ctx);
    }

	public static Promise<Proposal> selectRandomTalks() {
		 return F.Promise.promise(new F.Function0<Proposal>() {
	            @Override
	            public Proposal apply() throws Throwable {
	            	Long randomId = (long) (1 + Math.random() * (5-1));
	                return Proposal.find.byId(randomId);
	            }
	        }, ctx);
	}
}
