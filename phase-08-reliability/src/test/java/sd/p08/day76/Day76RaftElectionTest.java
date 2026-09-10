package sd.p08.day76;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class Day76RaftElectionTest {

    private static final int CLUSTER = 5;

    private static RaftNode node(int id) {
        return new RaftNode(id, CLUSTER);
    }

    @Test
    @DisplayName("nodes start as followers in term 0")
    void initialState() {
        RaftNode node = node(1);

        assertThat(node.state()).isEqualTo(NodeState.FOLLOWER);
        assertThat(node.currentTerm()).isZero();
        assertThat(node.votedFor()).isEmpty();
    }

    @Test
    @DisplayName("a majority of five is three")
    void majoritySize() {
        assertThat(new RaftNode(1, 5).majority()).isEqualTo(3);
        assertThat(new RaftNode(1, 3).majority()).isEqualTo(2);
        assertThat(new RaftNode(1, 4).majority())
                .as("even clusters still need a strict majority")
                .isEqualTo(3);
    }

    @Test
    @DisplayName("standing for election bumps the term and votes for self")
    void startingAnElection() {
        RaftNode candidate = node(1);

        candidate.startElection();

        assertThat(candidate.state()).isEqualTo(NodeState.CANDIDATE);
        assertThat(candidate.currentTerm()).isEqualTo(1);
        assertThat(candidate.votedFor()).hasValue(1);
        assertThat(candidate.votesReceived()).isEqualTo(1);
    }

    @Test
    @DisplayName("a follower grants a vote to a candidate in a newer term")
    void grantingAVote() {
        RaftNode follower = node(2);

        VoteResponse response = follower.handleVoteRequest(new VoteRequest(1, 1));

        assertThat(response.granted()).isTrue();
        assertThat(follower.currentTerm()).isEqualTo(1);
        assertThat(follower.votedFor()).hasValue(1);
    }

    @Test
    @DisplayName("ONE vote per term - this is what makes two leaders impossible")
    void oneVotePerTerm() {
        RaftNode follower = node(3);

        assertThat(follower.handleVoteRequest(new VoteRequest(1, 1)).granted()).isTrue();

        assertThat(follower.handleVoteRequest(new VoteRequest(2, 1)).granted())
                .as("""
                        A win needs a majority, and two majorities of the same set must overlap.
                        The overlapping node would have to vote twice in one term - so refusing
                        the second vote is exactly what guarantees at most one leader per term.""")
                .isFalse();
        assertThat(follower.votedFor()).hasValue(1);
    }

    @Test
    @DisplayName("a stale candidate is refused, and learns the real term")
    void staleCandidateIsRefused() {
        RaftNode node = node(2);
        node.handleVoteRequest(new VoteRequest(1, 5));

        VoteResponse response = node.handleVoteRequest(new VoteRequest(9, 3));

        assertThat(response.granted()).isFalse();
        assertThat(response.term())
                .as("the response carries our term, so the stale candidate can catch up")
                .isEqualTo(5);
    }

    @Test
    @DisplayName("a new term frees the vote")
    void newTermNewVote() {
        RaftNode follower = node(3);
        follower.handleVoteRequest(new VoteRequest(1, 1));

        assertThat(follower.handleVoteRequest(new VoteRequest(2, 2)).granted()).isTrue();
        assertThat(follower.votedFor()).hasValue(2);
    }

    @Test
    @DisplayName("a majority of votes makes a leader")
    void winningAnElection() {
        RaftNode candidate = node(1);
        candidate.startElection();

        candidate.receiveVote(new VoteResponse(1, true));
        assertThat(candidate.state())
                .as("two of five is not a majority")
                .isEqualTo(NodeState.CANDIDATE);

        candidate.receiveVote(new VoteResponse(1, true));

        assertThat(candidate.state()).isEqualTo(NodeState.LEADER);
        assertThat(candidate.votesReceived()).isEqualTo(3);
    }

    @Test
    @DisplayName("refused votes do not count")
    void refusedVotes() {
        RaftNode candidate = node(1);
        candidate.startElection();

        candidate.receiveVote(new VoteResponse(1, false));
        candidate.receiveVote(new VoteResponse(1, false));
        candidate.receiveVote(new VoteResponse(1, false));

        assertThat(candidate.state()).isEqualTo(NodeState.CANDIDATE);
        assertThat(candidate.votesReceived()).isEqualTo(1);
    }

    @Test
    @DisplayName("a higher term always wins - this is how a partitioned leader stands down")
    void higherTermForcesStepDown() {
        RaftNode leader = node(1);
        leader.startElection();
        leader.receiveVote(new VoteResponse(1, true));
        leader.receiveVote(new VoteResponse(1, true));
        assertThat(leader.state()).isEqualTo(NodeState.LEADER);

        // While it was partitioned away, the rest of the cluster elected somebody in term 7.
        leader.receiveHeartbeat(4, 7);

        assertThat(leader.state())
                .as("""
                        No global clock is needed. The term is a logical clock, and seeing a higher
                        one is proof you have been superseded. That single rule is how Raft
                        resolves split brain.""")
                .isEqualTo(NodeState.FOLLOWER);
        assertThat(leader.currentTerm()).isEqualTo(7);
        assertThat(leader.knownLeader()).hasValue(4);
    }

    @Test
    @DisplayName("a heartbeat from a stale leader is ignored")
    void staleHeartbeatIgnored() {
        RaftNode node = node(2);
        node.handleVoteRequest(new VoteRequest(1, 5));

        node.receiveHeartbeat(9, 2);

        assertThat(node.currentTerm()).isEqualTo(5);
        assertThat(node.knownLeader()).isEmpty();
    }

    @Test
    @DisplayName("a candidate that hears from a valid leader gives up")
    void candidateYieldsToLeader() {
        RaftNode candidate = node(2);
        candidate.startElection();

        candidate.receiveHeartbeat(1, candidate.currentTerm());

        assertThat(candidate.state()).isEqualTo(NodeState.FOLLOWER);
        assertThat(candidate.knownLeader()).hasValue(1);
    }

    @Test
    @DisplayName("a full five-node election elects exactly one leader")
    void wholeClusterElectsOneLeader() {
        List<RaftNode> cluster = List.of(node(0), node(1), node(2), node(3), node(4));
        RaftNode candidate = cluster.get(2);

        candidate.startElection();
        for (RaftNode peer : cluster) {
            if (peer != candidate) {
                candidate.receiveVote(peer.handleVoteRequest(
                        new VoteRequest(candidate.id(), candidate.currentTerm())));
            }
        }

        assertThat(candidate.state()).isEqualTo(NodeState.LEADER);
        assertThat(cluster.stream().filter(n -> n.state() == NodeState.LEADER).count())
                .as("exactly one leader per term, by construction")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("a split vote elects nobody - which is why timeouts are randomised")
    void splitVote() {
        List<RaftNode> voters = List.of(node(2), node(3), node(4));
        RaftNode a = node(0);
        RaftNode b = node(1);

        a.startElection();
        b.startElection();

        // Voters 2 and 3 reach A first; voter 4 reaches B first.
        a.receiveVote(voters.get(0).handleVoteRequest(new VoteRequest(0, 1)));
        b.receiveVote(voters.get(2).handleVoteRequest(new VoteRequest(1, 1)));
        a.receiveVote(voters.get(1).handleVoteRequest(new VoteRequest(0, 1)));

        assertThat(a.state()).isEqualTo(NodeState.LEADER);
        assertThat(b.state())
                .as("""
                        With unluckier timing neither would reach three and the term would elect
                        nobody at all. Randomised election timeouts make one node reliably go
                        first - Day 72's jitter, solving a different problem.""")
                .isEqualTo(NodeState.CANDIDATE);
    }
}
