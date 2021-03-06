# DEPRECATED - PLEASE USE <a href="https://github.com/idsc-frazzoli/owl">OWL</a> INSTEAD

The repository contains rapid prototype implementations of motion planners and their variants, version `0.1.0`.

The code includes experimental features that in their current form should not make their way to a live robot.
We have extracted and refined the approved methods to the repository <a href="https://github.com/idsc-frazzoli/owl">owl</a>.

List of algorithms: GLC, RRT*

The references are

* *A Generalized Label Correcting Method for Optimal Kinodynamic Motion Planning*
by Brian Paden and Emilio Frazzoli, 
[arXiv:1607.06966](https://arxiv.org/abs/1607.06966)
* *Sampling-based algorithms for optimal motion planning*
by Sertac Karaman and Emilio Frazzoli,
[IJRR11](http://ares.lids.mit.edu/papers/Karaman.Frazzoli.IJRR11.pdf)

The following integrators are available:

* Euler, Midpoint
* Runge-Kutta 4th order, and 5th order
* exact integrator for the group SE2

The `owly` repository implements visualizations in 2D as showcased below.
See also a [video](https://www.youtube.com/watch?v=lPQW3GqQqSY).

The separate repository [owly3d](https://github.com/idsc-frazzoli/owly3d) implements animations and visualizations in 3D.

## Examples

### GLC

Rice2: 4-dimensional state space + time

<table>
<tr>
<td>

![rice2dentity_1510227502495](https://user-images.githubusercontent.com/4012178/32603926-dd317aea-c54b-11e7-97ab-82df23b52fa5.gif)

<td>

![rice2dentity_1510234462100](https://user-images.githubusercontent.com/4012178/32608146-b6106d1c-c55b-11e7-918d-e0a1d1c8e400.gif)

</tr>
</table>

---

SE2: 3-dimensional state space

<table>
<tr>
<td>

Car

![se2entity_1510232282788](https://user-images.githubusercontent.com/4012178/32606961-813b05a6-c557-11e7-804c-83b1c5e94a6f.gif)

<td>

Two-wheel drive (with Lidar simulator)

![twdentity_1510751358909](https://user-images.githubusercontent.com/4012178/32838106-2d88fa2c-ca10-11e7-9c2a-68b34b1717cc.gif)

</tr>
</table>

---

Tracking of potential locations of pedestrians and vehicles

![shadow_region](https://user-images.githubusercontent.com/4012178/39653099-f9ed05c6-4fef-11e8-99d8-dc0515fca258.gif)

---

Pendulum Swing Up

![owly_psu1](https://user-images.githubusercontent.com/4012178/27012135-8979aae6-4eca-11e7-815e-95dd9b9ee0ea.png)

---

Rice1

![owly_rice1](https://user-images.githubusercontent.com/4012178/27012136-8979beaa-4eca-11e7-880f-7274c807c2b8.png)

---

R^2

![r2](https://cloud.githubusercontent.com/assets/4012178/25473192/c7cdd192-2b2e-11e7-8c9e-72d88d6723d3.png)

![owly_r2sphere](https://user-images.githubusercontent.com/4012178/27012137-897c3702-4eca-11e7-9665-72ffb87136ac.png)

---

Lotka-Volterra: predator and prey, control by decay rate of predators

![owly_1498564633644](https://user-images.githubusercontent.com/4012178/27586449-a90c1fc4-5b40-11e7-975b-9015f89ccfa3.png)

---

surface flow in river delta

![lava](https://cloud.githubusercontent.com/assets/4012178/26282194/6855b6d0-3e0c-11e7-92be-cb0ad99e3b8a.gif)

against the direction of current

![delta_c](https://cloud.githubusercontent.com/assets/4012178/26282183/3f750392-3e0c-11e7-95c6-2645545dbbc2.gif)

current reversed

![delta_s](https://cloud.githubusercontent.com/assets/4012178/26282191/59dafa84-3e0c-11e7-9602-2ece6f417bc1.gif)

### AnyTime GLC

R^2

![r2_circle_gif](https://user-images.githubusercontent.com/6703495/27226674-6a78071c-52a0-11e7-948c-7a12af42a7c1.gif)

### RRT*

R^2

![r2ani](https://cloud.githubusercontent.com/assets/4012178/26282173/06dccee8-3e0c-11e7-930f-fedab34fe396.gif)

![r2](https://cloud.githubusercontent.com/assets/4012178/26045794/16bd0a54-394c-11e7-9d11-19558bc3be88.png)

## Contributors

Jan Hakenberg, Jonas Londschien, Yannik Nager
