/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.api.process;

import baritone.api.IBaritone;
import baritone.api.behavior.IPathingBehavior;
import baritone.api.event.events.PathEvent;

/**
 * A process that can control the PathingBehavior.
 * <p>
 * Differences between a baritone process and a behavior:
 * Only one baritone process can be active at a time
 * PathingBehavior can only be controlled by a process
 * <p>
 * That's it actually
 *
 * @author leijurv
 */
public interface IBaritoneProcess {

    /**
     * Would this process like to be in control?
     *
     * @return Whether or not this process would like to be in contorl.
     */
    boolean isActive();

    /**
     * Called when this process is in control of pathing; Returns what Baritone should do.
     *
     * @param calcFailed     {@code true} if this specific process was in control last tick,
     *                       and there was a {@link PathEvent#CALC_FAILED} event last tick
     * @param isSafeToCancel {@code true} if a {@link PathingCommandType#REQUEST_PAUSE} would happen this tick, and
     *                       {@link IPathingBehavior} wouldn't actually tick. {@code false} if the PathExecutor reported
     *                       pausing would be unsafe at the end of the last tick. Effectively "could request cancel or
     *                       pause and have it happen right away"
     * @return What the {@link IPathingBehavior} should do
     */
    PathingCommand onTick(boolean calcFailed, boolean isSafeToCancel);

    /**
     * Returns whether or not this process should be treated as "temporary".
     * <p>
     * If a process is temporary, it doesn't call {@link #onLostControl} on the processes that aren't execute because of it.
     * <p>
     * For example, {@code CombatPauserProcess} and {@code PauseForAutoEatProcess} should return {@code true} always,
     * and should return {@link #isActive} {@code true} only if there's something in range this tick, or if the player would like
     * to start eating this tick. {@code PauseForAutoEatProcess} should only actually right click once onTick is called with
     * {@code isSafeToCancel} true though.
     *
     * @return Whethor or not if this control is temporary
     */
    boolean isTemporary();

    /**
     * Called if {@link #isActive} returned {@code true}, but another non-temporary
     * process has control. Effectively the same as cancel. You want control but you
     * don't get it.
     */
    void onLostControl();

    /**
     * Used to determine which Process gains control if multiple are reporting {@link #isActive()}. The one
     * that returns the highest value will be given control.
     *
     * @return A double representing the priority
     */
    double priority();

    /**
     * Returns which bot this process is associated with. (5000000iq forward thinking)
     *
     * @return The Bot associated with this process
     */
    IBaritone associatedWith();

    /**
     * Returns a user-friendly name for this process. Suitable for a HUD.
     *
     * @return A display name that's suitable for a HUD
     */
    String displayName();
}
