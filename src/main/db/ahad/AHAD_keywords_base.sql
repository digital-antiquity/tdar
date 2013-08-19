
--
-- TOC entry 2172 (class 0 OID 78993)
-- Dependencies: 163
-- Data for Name: culture_keyword; Type: TABLE DATA; Schema: public; Owner: tdar
--

COPY culture_keyword (id, definition, label, approved, index, selectable, parent_id) FROM stdin;
42	4201	Jewish	f	4.2	t	4
43		Peoples of the Sudan	f	4.3	t	4
49		Other North African and Middle Eastern	f	4.4	t	4
51		Mainland South-East Asian	f	5.1	t	5
52		Maritimme South-East Asian	f	5.2	t	5
61		Chinese Asian	f	6.1	t	6
2103	Affiliation with British people born or raised in Wales (ASCCEG 2103)	Welsh	t	2.1.3	t	21
69		Other North-East Asian	f	6.2	t	6
71		Southern Asian	f	7.1	t	7
72		Central Asian	f	7.2	t	7
91		Central and West African	f	9.1	t	9
92		Southern and East African	f	9.2	t	9
23	Austrian, Dutch, Flemish, French, German, SwissBelgian and other groups (ASCCEG 23)	Western European	t	2.3	t	2
24	Danish, Finnish, Icelandic, Norwegian, Swedish  (ASCCEG 24)	Northern European	t	2.4	t	2
31	Italian, Portuguese, Spanish among others  (ASCCEG 31)	Southern European	t	3.1	t	3
32	Albanian, Bosnian, Greek and other groups (ASCCEG 32)	South Eastern European	t	3.2	t	3
33	Czechoslovakian, Polish, Russian and other groups (ASCCEG 33)	Eastern European	t	3.3	t	3
4	Includes Arab ethnic groups (Algerian, Egyptian, Iraqi among others), Jewish, Peoples of the Sudan, Turkish, Coptic, Assyrian and other groups (ASCCEG 4)	North African and Middle Eastern	t	4	t	\N
5	Burmese, Thai, Cambodian, Indonesian and others (ASCCEG 5)	South-East Asian	t	5	t	\N
6	Chinese, Taiwanese, Japanese, Korean and others(ASCCEG 6)	North-East Asian	t	6	t	\N
7	Indian, Sri Lankan, Pakistani, Afghani and others (ASCCEG 7)	Southern and Central Asian	t	7	t	\N
1		Oceania	t	1	t	\N
2		North-West European	t	2	t	\N
3		Southern and Eastern European	t	3	t	\N
8		People of the Americas	t	8	t	\N
11		Australian Peoples	t	1.1	t	1
12		New Zealand Peoples	t	1.2	t	1
13		Melanesian and Papuan	f	1.3	t	1
14		Micronesian	f	1.4	t	1
15		Polynesian	f	1.5	t	1
21		British	t	2.1	t	2
1101	Includes affiliation with present day and colonial (pre-1901) Australian culture (ASCCEG 1101, as adapted by AHAD)	Australian	t	1.1.1	t	11
1102	Includes affiliation with Aboriginal peoples throughout Australia (ASCCEG 1102)	Australian Aboriginal	t	1.1.2	t	11
1103	Includes affiliation with South Sea Islander people throughout Australia (ASCCEG 1103)	South Sea Islanderr	t	1.1.3	t	11
1104	Includes affiliation with Torres Strait Islander people (ASCCEG 1104)	Torres Strait Islander	t	1.1.4	t	11
1201	Includes affiliation with all Maori people through New Zealand (ASCCEG 1201)	Maori	t	1.2.1	t	12
1202	Includes affiliation with present day and colonial (pre-1907) New Zealand culture (ASCCEG 1202, as adapted by AHAD)	New Zealander	t	1.2.2	t	12
81	American, Canadian, French Canadian, African American, Hispanic (North American), Native North American Indian, Bermudan (ASCCEG 81)	North America	t	8.1	t	8
9	Includes Central, West, South and East African ethnic groups (ASCCEG 9)	Sub-Saharan African	t	9	t	\N
2101	Affiliation with British people born or raised in England (ASCCEG 2101)	English	t	2.1.1	t	21
2102	Affiliation with British people born or raised in Scotland (ASCCEG 2102)	Scottish	t	2.1.2	t	21
22	Affiliation with British people born or raised in Ireland (ASCCEG 22, as adapted by AHAD)	Irish	t	2.2	t	2
82	Argentinian, Bolivian, Brazilian, Chilean, Colombian, Ecuadorian, Peruvian, Uruguayan, Venezuelan and other groups (ASCCEG 82)	South American	t	8.2	t	8
83	Mexican, Nicaraguan, Salvadorian, Costa Rican, Guatemalan, Mayan, and other groups (ASCCEG 83)	Central American	t	8.3	t	8
41		Arab	f	4.1	t	4
84	Cuban, Jamaican, Tobagonian, Puerto Rican and other groups (ASCCEG 84)	Carribean Islander	t	8.4	t	8
\.


--
-- TOC entry 2173 (class 0 OID 79050)
-- Dependencies: 176
-- Data for Name: geographic_keyword; Type: TABLE DATA; Schema: public; Owner: tdar
--

COPY geographic_keyword (id, definition, label, level) FROM stdin;
1	\N	Andes	\N
2	\N	Arizona	\N
3	\N	Basin of Mexico	\N
4	\N	Bolivia	\N
5	\N	Cape Cod	\N
6	\N	Central Mexico, Otumba	\N
7	\N	Central Mexico, Teotihuacan Valley	\N
8	\N	Cibola	\N
9	\N	Eastern Mimbres	\N
10	\N	Eastham	\N
11	\N	El Morro Valley	\N
12	\N	G1	\N
13	\N	G2	\N
14	\N	Green County, Illinois	\N
15	\N	Hohokam	\N
16	\N	Kalahari Desert	\N
17	\N	Lake Titicaca	\N
18	\N	Lyman Lake State Park	\N
19	\N	Mesoamerican Northern Periphery	\N
20	\N	Mimbres Valley	\N
21	\N	New Mexico	\N
22	\N	North America	\N
23	\N	North Truro	\N
24	\N	North West Mexico	\N
25	\N	Norway	\N
26	\N	Outer Cape Cod	\N
27	\N	Phoenix Basin	\N
28	\N	Provincelands	\N
29	\N	Provincetown	\N
30	\N	Rio Grande	\N
31	\N	Salinas	\N
32	\N	Section 21, T 9 N, R 13 W Greene County in the lower Illinois Valley	\N
33	\N	South America	\N
34	\N	South-Central Andes	\N
35	\N	Southeastern Massachusetts	\N
36	\N	Southern New England	\N
37	\N	Taraco Peninsula	\N
38	\N	The United States	\N
39	\N	Titicaca Basin	\N
40	\N	Truro	\N
41	\N	Upper Gila	\N
42	\N	U.S. Midwest	\N
43	\N	U.S. Southwest	\N
44	\N	Valley of Mexico	\N
45	\N	Wellfleet	\N
46	\N	Zuni Indian Reservation	\N
47	\N		\N
48	\N	New England	\N
49	\N	Eastern Canada	\N
50	\N	Cape Cod National Seashore	\N
51	\N	Barnstable County	\N
52	\N	Nauset Marsh	\N
53	\N	Zuni	\N
54	\N	Upper Little Colorado River	\N
55	\N	Eats Yorkshire	\N
56	\N	East Yorkshire	\N
57	\N	Alexandria, Virginia	\N
58	\N	New Philadelphia, Pike County, Illinois	\N
59	\N	Tierra Firma	\N
60	\N	Castilla de Oro 	\N
61	\N	Mesoamerica	\N
62	\N	Chiriquí 	\N
63	\N	Panama	\N
64	\N	Barú volcano	\N
65	\N	Talamanca Cordillera	\N
66	\N	Bay of Chiriquí 	\N
67	\N	Pacific Ocean 	\N
68	\N	New World 	\N
69	\N	New York City Port 	\N
70	\N	Isthmus of Panamá 	\N
71	\N	California	\N
72	\N	Terraba	\N
73	\N	Boruca	\N
74	\N	Santiago de Veraguas	\N
75	\N	Panamá City 	\N
76	\N	City of David	\N
77	\N	City of Gold	\N
78	\N	twentieth century 	\N
79	\N	Boquete	\N
80	\N	Barú region 	\N
81	\N	Chiriquí Province	\N
82	\N	Rincon Mountains	\N
83	\N	Pima County	\N
84	\N	Chiricahua Mountains	\N
85	\N	Sulphur Springs Valley	\N
86	\N	Wawona Valley	\N
87	\N	Meso America	\N
88	\N	Death Valley	\N
89	\N	Nevada	\N
90	\N	Molokai	\N
91	\N	Hawaii	\N
92	\N	Tonto Basin	\N
93	\N	Mohave Desert	\N
94	\N	Big Island	\N
95	\N	Upper Gila River	\N
96	\N	Walnut Canyon	\N
97	\N	Coconino County	\N
98	\N	Verde Valley	\N
99	\N	Yavapai County	\N
100	\N	Lake Mead	\N
101	\N	Mariana Islands	\N
102	\N	Commonwealth of the Northern Marianas	\N
103	\N	Micronesia	\N
104	\N	Pacific	\N
105	\N	Commonwealth of the Northern Mariana Islands	\N
106	\N	Snake Valley	\N
107	\N	Northern Belize	\N
108	\N	Shivwits Plateau	\N
109	\N	Inyo County	\N
110	\N	Owens Valley	\N
111	\N	Black Canyon	\N
112	\N	Montrose County	\N
113	\N	Colorado	\N
114	\N	Tucson Mountains	\N
115	\N	Eureka Valley	\N
116	\N	Idaho	\N
117	\N	Glen Canyon	\N
118	\N	Black Kettle National Grassland	\N
119	\N	Roger Mills County	\N
120	\N	Oklahoma	\N
121	\N	Scotty's Castle	\N
122	\N	Tie Canyon	\N
123	\N	Johnson Canyon	\N
124	\N	Grand Canyon	\N
125	\N	Lower Salt River	\N
126	\N	Phoenix	\N
127	\N	Salt River	\N
128	\N	Mesa Verde	\N
129	\N	New World	\N
130	\N	Greene County	\N
131	\N	Calhoun County	\N
132	\N	Illinois	\N
133	\N	Massachusetts	\N
134	\N	Mexico	\N
135	\N	Central Mexico	\N
136	\N	West Mexico	\N
137	\N	US Southwest	\N
138	\N	Eastern North America	\N
139	\N	Eastern Woodlands	\N
140	\N	US Plains	\N
141	\N	US Southeast	\N
142	\N	US Midwest	\N
143	\N	Yucatan Peninsula	\N
144	\N	Greater Southwest	\N
145	\N	Southern Maya Lowlands	\N
146	\N	Mogollon	\N
147	\N	Anasazi	\N
148	\N	Northern Mexico	\N
149	\N	American Southwest	\N
150	\N	American Southeast	\N
151	\N	US Eastern Woodlands	\N
152	\N	US Great Plains	\N
153	\N	Gulf Coast	\N
154	\N	Teotihuacan Valley	\N
155	\N	Chiapas	\N
156	\N	Soconusco region	\N
157	\N	Pacific coast of Guatemala	\N
158	\N	Pacific coast of Chiapas	\N
159	\N	Southern Gulf Lowlands	\N
160	\N	Puebla	\N
161	\N	Tuxcala	\N
162	\N	Mohawk Valley	\N
163	\N	Northeastern North America	\N
164	\N	Tlaxcala	\N
165	\N	Arkansas	\N
166	\N	Wisconsin	\N
167	\N	Minnesota	\N
168	\N	Missouri	\N
169	\N	Tennessee	\N
170	\N	Spiro	\N
171	\N	Cahokia	\N
172	\N	Georgia	\N
173	\N	Etowah	\N
174	\N	New York	\N
175	\N	Manhattan	\N
176	\N	Greenland	\N
177	\N	Iceland	\N
178	\N	Shetland Islands	\N
179	\N	Alabama	\N
180	\N	Mussel Shoals	\N
181	\N	Southeast	\N
182	\N	Aztalan	\N
183	\N	St. Louis	\N
184	\N	Florida	\N
185	\N	Jacksonville	\N
186	\N	Iowa County	\N
187	\N	St. Clair County	\N
188	\N	Pike County	\N
189	\N	Fulton County	\N
190	\N	Midwest	\N
191	\N	Iowa	\N
192	\N	De Soto Parish 	\N
193	\N	Louisiana	\N
194	\N	Pierce County	\N
195	\N	Middle Mississippian	\N
196	\N	southeast	\N
197	\N	midwest	\N
198	\N	Union County	\N
199	\N	Southwest Wisconsin	\N
200	\N	Illlinois	\N
201	\N	Picture Cave	\N
202	\N	Teotihuacan	\N
203	\N	Dunklin County	\N
204	\N	Mississippi River	\N
205	\N	Muscatine Slough	\N
214	\N	bolivia	\N
215	\N	andes	\N
216	\N	titicaca basin	\N
217	\N	altiplano	\N
218	\N	taraco peninsula	\N
219	\N	lower Verde River	\N
220	\N	central Arizona	\N
221	\N	southern Arizona	\N
222	\N	Verde River	\N
223	\N	Tonto National Forest	\N
224	\N	Agua Fria River	\N
225	\N	Prescott area	\N
226	\N	Payson area	\N
227	\N	Perry Mesa	\N
229	\N	Middle Verde Valley, Arizona	\N
230	\N	middle Verde Valley, Arizona	\N
231	\N	northern Southwest	\N
232	\N	Agua Fria National Monument	\N
233	\N	Fort Apache Reservation	\N
234	\N	San Carlos Reservation	\N
235	\N	Fort McDowell Reservation	\N
236	\N	Yavapai Apache Reservation	\N
237	\N	Yavapai Prescott Reservation	\N
238	\N	Tonto Apache Reservation	\N
239	\N	Horseshoe Basin	\N
240	\N	Ister Flat	\N
241	\N	Mullen Mesa	\N
242	\N	Lime Creek area	\N
243	\N	McCoy Mesa	\N
244	\N	Davenport Wash area	\N
245	\N	Gila County (County)	COUNTY
246	\N	Midland County (County)	COUNTY
247	\N	Cibola County (County)	COUNTY
248	\N	Menard County (County)	COUNTY
249	\N	Tamaulipas (State / Territory)	STATE
250	\N	El Paso County (County)	COUNTY
251	\N	Eddy County (County)	COUNTY
252	\N	Eastland County (County)	COUNTY
253	\N	Socorro County (County)	COUNTY
254	\N	King County (County)	COUNTY
255	\N	McKinley County (County)	COUNTY
256	\N	Upton County (County)	COUNTY
257	\N	Nolan County (County)	COUNTY
258	\N	Comal County (County)	COUNTY
259	\N	MX (ISO Country Code)	ISO_COUNTRY
260	\N	Coahuila (State / Territory)	STATE
261	\N	Mason County (County)	COUNTY
262	\N	Archer County (County)	COUNTY
263	\N	Beckham County (County)	COUNTY
264	\N	Hidalgo (State / Territory)	STATE
265	\N	Winkler County (County)	COUNTY
266	\N	Haskell County (County)	COUNTY
267	\N	Brewster County (County)	COUNTY
268	\N	Kimble County (County)	COUNTY
269	\N	Cochran County (County)	COUNTY
270	\N	Dawson County (County)	COUNTY
271	\N	Runnels County (County)	COUNTY
272	\N	Lubbock County (County)	COUNTY
273	\N	Swisher County (County)	COUNTY
274	\N	Shackelford County (County)	COUNTY
275	\N	Jim Hogg County (County)	COUNTY
276	\N	US (ISO Country Code)	ISO_COUNTRY
277	\N	Medina County (County)	COUNTY
278	\N	Martin County (County)	COUNTY
279	\N	Baja California Sur (State / Territory)	STATE
280	\N	Castro County (County)	COUNTY
281	\N	Gray County (County)	COUNTY
282	\N	Bandera County (County)	COUNTY
283	\N	Motley County (County)	COUNTY
284	\N	Maverick County (County)	COUNTY
285	\N	Pima County (County)	COUNTY
286	\N	Bexar County (County)	COUNTY
287	\N	Andrews County (County)	COUNTY
288	\N	Colima (State / Territory)	STATE
289	\N	Knox County (County)	COUNTY
290	\N	Hale County (County)	COUNTY
291	\N	Mitchell County (County)	COUNTY
292	\N	Dickens County (County)	COUNTY
293	\N	Navajo County (County)	COUNTY
294	\N	Roger Mills County (County)	COUNTY
295	\N	Grant County (County)	COUNTY
296	\N	Wilbarger County (County)	COUNTY
297	\N	Harding County (County)	COUNTY
298	\N	Wheeler County (County)	COUNTY
299	\N	New Mexico (State / Territory)	STATE
300	\N	Callahan County (County)	COUNTY
301	\N	Caddo County (County)	COUNTY
302	\N	Randall County (County)	COUNTY
303	\N	Foard County (County)	COUNTY
304	\N	Stephens County (County)	COUNTY
305	\N	Taylor County (County)	COUNTY
306	\N	Fisher County (County)	COUNTY
307	\N	Crockett County (County)	COUNTY
308	\N	Santa Fe County (County)	COUNTY
309	\N	Queretaro (State / Territory)	STATE
310	\N	Presidio County (County)	COUNTY
311	\N	Roosevelt County (County)	COUNTY
312	\N	Otero County (County)	COUNTY
313	\N	Coconino County (County)	COUNTY
314	\N	Lynn County (County)	COUNTY
315	\N	Hardeman County (County)	COUNTY
316	\N	Jones County (County)	COUNTY
317	\N	Tillman County (County)	COUNTY
318	\N	Curry County (County)	COUNTY
319	\N	Reeves County (County)	COUNTY
320	\N	Hockley County (County)	COUNTY
321	\N	Gillespie County (County)	COUNTY
322	\N	Michoacan (State / Territory)	STATE
323	\N	Throckmorton County (County)	COUNTY
324	\N	Stonewall County (County)	COUNTY
325	\N	Borden County (County)	COUNTY
326	\N	Mexico (State / Territory)	STATE
327	\N	Yavapai County (County)	COUNTY
328	\N	Kendall County (County)	COUNTY
329	\N	Zavala County (County)	COUNTY
330	\N	Oklahoma (State / Territory)	STATE
331	\N	McMullen County (County)	COUNTY
332	\N	Kerr County (County)	COUNTY
333	\N	Jackson County (County)	COUNTY
334	\N	Bailey County (County)	COUNTY
335	\N	Arizona (State / Territory)	STATE
336	\N	Ward County (County)	COUNTY
337	\N	Bernalillo County (County)	COUNTY
338	\N	Washita County (County)	COUNTY
339	\N	Young County (County)	COUNTY
340	\N	Starr County (County)	COUNTY
341	\N	Cochise County (County)	COUNTY
342	\N	Pecos County (County)	COUNTY
343	\N	Brown County (County)	COUNTY
344	\N	Frio County (County)	COUNTY
345	\N	Culberson County (County)	COUNTY
346	\N	Sinaloa (State / Territory)	STATE
347	\N	Crane County (County)	COUNTY
348	\N	Texas (State / Territory)	STATE
349	\N	San Luis Potosi (State / Territory)	STATE
350	\N	Comanche County (County)	COUNTY
351	\N	Parmer County (County)	COUNTY
352	\N	Sonora (State / Territory)	STATE
353	\N	Morelos (State / Territory)	STATE
354	\N	Valencia County (County)	COUNTY
355	\N	Irion County (County)	COUNTY
356	\N	Crosby County (County)	COUNTY
357	\N	Jalisco (State / Territory)	STATE
358	\N	Dimmit County (County)	COUNTY
359	\N	Real County (County)	COUNTY
360	\N	Mills County (County)	COUNTY
361	\N	Baylor County (County)	COUNTY
362	\N	Luna County (County)	COUNTY
363	\N	Hudspeth County (County)	COUNTY
364	\N	Kinney County (County)	COUNTY
365	\N	Guerrero (State / Territory)	STATE
366	\N	Zacatecas (State / Territory)	STATE
367	\N	Armstrong County (County)	COUNTY
368	\N	Sierra County (County)	COUNTY
369	\N	Quay County (County)	COUNTY
370	\N	Edwards County (County)	COUNTY
371	\N	Greer County (County)	COUNTY
372	\N	Glasscock County (County)	COUNTY
373	\N	Reagan County (County)	COUNTY
374	\N	Deaf Smith County (County)	COUNTY
375	\N	Duval County (County)	COUNTY
376	\N	Oldham County (County)	COUNTY
377	\N	Nayarit (State / Territory)	STATE
378	\N	Aguascalientes (State / Territory)	STATE
379	\N	Donley County (County)	COUNTY
380	\N	Guanajuato (State / Territory)	STATE
381	\N	Puebla (State / Territory)	STATE
382	\N	Childress County (County)	COUNTY
383	\N	Sandoval County (County)	COUNTY
384	\N	United States of America (Country)	COUNTRY
385	\N	Hall County (County)	COUNTY
386	\N	Sterling County (County)	COUNTY
387	\N	Hidalgo County (County)	COUNTY
388	\N	Tom Green County (County)	COUNTY
389	\N	Doña Ana County (County)	COUNTY
390	\N	Tlaxcala (State / Territory)	STATE
391	\N	Gaines County (County)	COUNTY
392	\N	Lea County (County)	COUNTY
393	\N	Jeff Davis County (County)	COUNTY
394	\N	Santa Cruz County (County)	COUNTY
395	\N	Schleicher County (County)	COUNTY
396	\N	Graham County (County)	COUNTY
397	\N	Garza County (County)	COUNTY
398	\N	Uvalde County (County)	COUNTY
399	\N	Guadalupe County (County)	COUNTY
400	\N	Coke County (County)	COUNTY
401	\N	Concho County (County)	COUNTY
402	\N	De Baca County (County)	COUNTY
403	\N	Kent County (County)	COUNTY
404	\N	Chaves County (County)	COUNTY
405	\N	Webb County (County)	COUNTY
406	\N	Chihuahua (State / Territory)	STATE
407	\N	Sutton County (County)	COUNTY
408	\N	Apache County (County)	COUNTY
409	\N	Durango (State / Territory)	STATE
410	\N	Loving County (County)	COUNTY
411	\N	San Miguel County (County)	COUNTY
412	\N	Coleman County (County)	COUNTY
413	\N	Briscoe County (County)	COUNTY
414	\N	Atascosa County (County)	COUNTY
415	\N	Wichita County (County)	COUNTY
416	\N	Catron County (County)	COUNTY
417	\N	Cotton County (County)	COUNTY
418	\N	Terry County (County)	COUNTY
419	\N	Yoakum County (County)	COUNTY
420	\N	Cottle County (County)	COUNTY
421	\N	Howard County (County)	COUNTY
422	\N	Distrito Federal (State / Territory)	STATE
423	\N	Torrance County (County)	COUNTY
424	\N	Pinal County (County)	COUNTY
425	\N	San Saba County (County)	COUNTY
426	\N	Collingsworth County (County)	COUNTY
427	\N	Carson County (County)	COUNTY
428	\N	Lincoln County (County)	COUNTY
429	\N	Val Verde County (County)	COUNTY
430	\N	Nuevo Leon (State / Territory)	STATE
431	\N	Zapata County (County)	COUNTY
432	\N	Veracruz (State / Territory)	STATE
433	\N	Floyd County (County)	COUNTY
434	\N	Llano County (County)	COUNTY
435	\N	McCulloch County (County)	COUNTY
436	\N	Potter County (County)	COUNTY
437	\N	Kiowa County (County)	COUNTY
438	\N	United Mexican States (Country)	COUNTRY
439	\N	Maricopa County (County)	COUNTY
440	\N	Greenlee County (County)	COUNTY
441	\N	Lamb County (County)	COUNTY
442	\N	Harmon County (County)	COUNTY
443	\N	Scurry County (County)	COUNTY
444	\N	La Salle County (County)	COUNTY
445	\N	Ector County (County)	COUNTY
446	\N	Terrell County (County)	COUNTY
447	\N	Hart County (County)	COUNTY
448	\N	Pottawatomie County (County)	COUNTY
449	\N	Gonzales County (County)	COUNTY
450	\N	Beauregard Parish (County)	COUNTY
451	\N	Antelope County (County)	COUNTY
452	\N	Dyer County (County)	COUNTY
453	\N	Cape Girardeau County (County)	COUNTY
454	\N	Hansford County (County)	COUNTY
455	\N	Roberts County (County)	COUNTY
456	\N	Leake County (County)	COUNTY
457	\N	Orangeburg County (County)	COUNTY
458	\N	Saint Catherine (State / Territory)	STATE
459	\N	Windsor County (County)	COUNTY
460	\N	Palm Beach County (County)	COUNTY
461	\N	Morris County (County)	COUNTY
462	\N	Tama County (County)	COUNTY
463	\N	Giles County (County)	COUNTY
464	\N	Des Moines County (County)	COUNTY
465	\N	Lackawanna County (County)	COUNTY
466	\N	Morrill County (County)	COUNTY
467	\N	Cayman Is. (State / Territory)	STATE
468	\N	Inyo County (County)	COUNTY
469	\N	Rawlins County (County)	COUNTY
470	\N	San Augustine County (County)	COUNTY
471	\N	Guayanilla Municipio (County)	COUNTY
472	\N	Pittsylvania County (County)	COUNTY
473	\N	Redwood County (County)	COUNTY
474	\N	Colquitt County (County)	COUNTY
475	\N	Fergus County (County)	COUNTY
476	\N	Baja Verapaz (State / Territory)	STATE
477	\N	Lucas County (County)	COUNTY
478	\N	Ontario County (County)	COUNTY
479	\N	Maury County (County)	COUNTY
480	\N	Passaic County (County)	COUNTY
481	\N	Saratoga County (County)	COUNTY
482	\N	Lares Municipio (County)	COUNTY
483	\N	Costilla County (County)	COUNTY
484	\N	Hawaii County (County)	COUNTY
485	\N	Pasquotank County (County)	COUNTY
486	\N	Mobile County (County)	COUNTY
487	\N	Missoula County (County)	COUNTY
488	\N	Bonner County (County)	COUNTY
489	\N	Pointe Coupee Parish (County)	COUNTY
490	\N	Grant Parish (County)	COUNTY
491	\N	Augusta County (County)	COUNTY
492	\N	Alberta (State / Territory)	STATE
493	\N	Kittitas County (County)	COUNTY
494	\N	Bossier Parish (County)	COUNTY
495	\N	Stanley County (County)	COUNTY
496	\N	San Sebastián Municipio (County)	COUNTY
497	\N	Morehouse Parish (County)	COUNTY
498	\N	Juneau City and Borough (County)	COUNTY
499	\N	Williamsburg County (County)	COUNTY
500	\N	Sacatepequez (State / Territory)	STATE
501	\N	Jefferson County (County)	COUNTY
502	\N	Le Sueur County (County)	COUNTY
503	\N	Delaware (State / Territory)	STATE
504	\N	Baltimore city (County)	COUNTY
505	\N	Brooke County (County)	COUNTY
506	\N	Aguada Municipio (County)	COUNTY
507	\N	Sandusky County (County)	COUNTY
508	\N	Artibonite (State / Territory)	STATE
509	\N	Connecticut (State / Territory)	STATE
510	\N	East Baton Rouge Parish (County)	COUNTY
511	\N	Garrard County (County)	COUNTY
512	\N	Siskiyou County (County)	COUNTY
513	\N	Georgia (State / Territory)	STATE
514	\N	Kimball County (County)	COUNTY
515	\N	Buncombe County (County)	COUNTY
516	\N	Grenada County (County)	COUNTY
517	\N	Parke County (County)	COUNTY
518	\N	Lycoming County (County)	COUNTY
519	\N	Arecibo Municipio (County)	COUNTY
520	\N	Clarion County (County)	COUNTY
521	\N	Culebra Municipio (County)	COUNTY
522	\N	Cayey Municipio (County)	COUNTY
523	\N	West Carroll Parish (County)	COUNTY
524	\N	Hendry County (County)	COUNTY
525	\N	Cattaraugus County (County)	COUNTY
526	\N	Colorado (State / Territory)	STATE
527	\N	Tioga County (County)	COUNTY
528	\N	Nord-Ouest (State / Territory)	STATE
529	\N	Warrick County (County)	COUNTY
530	\N	Warren County (County)	COUNTY
531	\N	Seward County (County)	COUNTY
532	\N	Santa Isabel Municipio (County)	COUNTY
533	\N	Nunavut (State / Territory)	STATE
534	\N	Massachusetts (State / Territory)	STATE
535	\N	Haywood County (County)	COUNTY
536	\N	Highland County (County)	COUNTY
537	\N	Converse County (County)	COUNTY
538	\N	Gooding County (County)	COUNTY
539	\N	Roscommon County (County)	COUNTY
540	\N	Champaign County (County)	COUNTY
541	\N	Loup County (County)	COUNTY
542	\N	Lowndes County (County)	COUNTY
543	\N	Colusa County (County)	COUNTY
544	\N	Honolulu County (County)	COUNTY
545	\N	La Crosse County (County)	COUNTY
546	\N	Defiance County (County)	COUNTY
547	\N	Mississippi (State / Territory)	STATE
548	\N	Geneva County (County)	COUNTY
549	\N	Indian River County (County)	COUNTY
550	\N	Litchfield County (County)	COUNTY
551	\N	Penobscot County (County)	COUNTY
552	\N	Southampton County (County)	COUNTY
553	\N	Pondera County (County)	COUNTY
554	\N	Ciales Municipio (County)	COUNTY
555	\N	Anchorage Municipality (County)	COUNTY
556	\N	Maries County (County)	COUNTY
557	\N	Boise County (County)	COUNTY
558	\N	Lyon County (County)	COUNTY
559	\N	Garvin County (County)	COUNTY
560	\N	Allamakee County (County)	COUNTY
561	\N	Manchester (State / Territory)	STATE
562	\N	Cimarron County (County)	COUNTY
563	\N	Gates County (County)	COUNTY
564	\N	Juana Díaz Municipio (County)	COUNTY
565	\N	Sequatchie County (County)	COUNTY
566	\N	Garden County (County)	COUNTY
567	\N	Marathon County (County)	COUNTY
568	\N	Braxton County (County)	COUNTY
569	\N	Washoe County (County)	COUNTY
570	\N	Pleasants County (County)	COUNTY
571	\N	Cabanas (State / Territory)	STATE
572	\N	Posey County (County)	COUNTY
573	\N	Addison County (County)	COUNTY
574	\N	Islas de la Bahia (State / Territory)	STATE
575	\N	Stokes County (County)	COUNTY
576	\N	Habersham County (County)	COUNTY
577	\N	Bristol County (County)	COUNTY
578	\N	Petroleum County (County)	COUNTY
579	\N	Coamo Municipio (County)	COUNTY
580	\N	Logan County (County)	COUNTY
581	\N	Merrick County (County)	COUNTY
582	\N	NI (ISO Country Code)	ISO_COUNTRY
583	\N	Bland County (County)	COUNTY
584	\N	Alexander County (County)	COUNTY
585	\N	Guadeloupe (State / Territory)	STATE
586	\N	Catawba County (County)	COUNTY
587	\N	Matanuska-Susitna Borough (County)	COUNTY
588	\N	Stanly County (County)	COUNTY
589	\N	MQ (ISO Country Code)	ISO_COUNTRY
590	\N	Whiteside County (County)	COUNTY
591	\N	Tucker County (County)	COUNTY
592	\N	St. Charles Parish (County)	COUNTY
593	\N	Boone County (County)	COUNTY
594	\N	Sequoyah County (County)	COUNTY
595	\N	El Paraiso (State / Territory)	STATE
596	\N	Routt County (County)	COUNTY
597	\N	New York (State / Territory)	STATE
598	\N	Clarendon (State / Territory)	STATE
599	\N	Queens County (County)	COUNTY
600	\N	Pickett County (County)	COUNTY
601	\N	Swift County (County)	COUNTY
602	\N	Cataño Municipio (County)	COUNTY
603	\N	Gosper County (County)	COUNTY
604	\N	Uinta County (County)	COUNTY
605	\N	Wasco County (County)	COUNTY
606	\N	Dorchester County (County)	COUNTY
607	\N	Wallace County (County)	COUNTY
608	\N	Anson County (County)	COUNTY
609	\N	Humacao Municipio (County)	COUNTY
610	\N	Auglaize County (County)	COUNTY
611	\N	Bon Homme County (County)	COUNTY
612	\N	Josephine County (County)	COUNTY
613	\N	Portsmouth city (County)	COUNTY
614	\N	Jay County (County)	COUNTY
615	\N	Allendale County (County)	COUNTY
616	\N	Samana (State / Territory)	STATE
617	\N	Hodgeman County (County)	COUNTY
618	\N	Johnston County (County)	COUNTY
619	\N	Kanawha County (County)	COUNTY
620	\N	Kalkaska County (County)	COUNTY
621	\N	Baker County (County)	COUNTY
622	\N	Dougherty County (County)	COUNTY
623	\N	Elko County (County)	COUNTY
624	\N	Moody County (County)	COUNTY
625	\N	Meigs County (County)	COUNTY
626	\N	Early County (County)	COUNTY
627	\N	Breathitt County (County)	COUNTY
628	\N	Gunnison County (County)	COUNTY
629	\N	Northumberland County (County)	COUNTY
630	\N	Banner County (County)	COUNTY
631	\N	Monmouth County (County)	COUNTY
632	\N	Branch County (County)	COUNTY
633	\N	Camaguey (State / Territory)	STATE
634	\N	Alleghany County (County)	COUNTY
635	\N	Bertie County (County)	COUNTY
636	\N	Thurston County (County)	COUNTY
637	\N	Dickson County (County)	COUNTY
638	\N	Lawrence County (County)	COUNTY
639	\N	Worcester County (County)	COUNTY
640	\N	Lyman County (County)	COUNTY
641	\N	Guernsey County (County)	COUNTY
642	\N	Casey County (County)	COUNTY
643	\N	McCreary County (County)	COUNTY
644	\N	Tehama County (County)	COUNTY
645	\N	Harney County (County)	COUNTY
646	\N	Calumet County (County)	COUNTY
647	\N	Manitowoc County (County)	COUNTY
648	\N	Ohio County (County)	COUNTY
649	\N	Texas County (County)	COUNTY
650	\N	Crow Wing County (County)	COUNTY
651	\N	Madera County (County)	COUNTY
652	\N	Troup County (County)	COUNTY
653	\N	Bayfield County (County)	COUNTY
654	\N	Bureau County (County)	COUNTY
655	\N	Spartanburg County (County)	COUNTY
656	\N	Hatillo Municipio (County)	COUNTY
657	\N	Montague County (County)	COUNTY
658	\N	Del Norte County (County)	COUNTY
659	\N	Independence County (County)	COUNTY
660	\N	St. James Parish (County)	COUNTY
661	\N	Raleigh County (County)	COUNTY
662	\N	Lincoln Parish (County)	COUNTY
663	\N	Okmulgee County (County)	COUNTY
664	\N	Bethel Census Area (County)	COUNTY
665	\N	Juab County (County)	COUNTY
666	\N	Toombs County (County)	COUNTY
667	\N	Department of Martinique (Country)	COUNTRY
668	\N	Tate County (County)	COUNTY
669	\N	Dukes County (County)	COUNTY
670	\N	Kershaw County (County)	COUNTY
671	\N	Cross County (County)	COUNTY
672	\N	Tallapoosa County (County)	COUNTY
673	\N	Rockdale County (County)	COUNTY
674	\N	Peten (State / Territory)	STATE
675	\N	Louisa County (County)	COUNTY
676	\N	Haralson County (County)	COUNTY
677	\N	Tulsa County (County)	COUNTY
678	\N	KY (ISO Country Code)	ISO_COUNTRY
679	\N	Jewell County (County)	COUNTY
680	\N	Maricao Municipio (County)	COUNTY
681	\N	Lunenburg County (County)	COUNTY
682	\N	Kalawao County (County)	COUNTY
683	\N	Pittsburg County (County)	COUNTY
684	\N	Chesterfield County (County)	COUNTY
685	\N	Yukon Territory (State / Territory)	STATE
686	\N	Missouri (State / Territory)	STATE
687	\N	Monona County (County)	COUNTY
688	\N	Vigo County (County)	COUNTY
689	\N	Barren County (County)	COUNTY
690	\N	Magoffin County (County)	COUNTY
691	\N	Mohave County (County)	COUNTY
692	\N	Deer Lodge County (County)	COUNTY
693	\N	Klickitat County (County)	COUNTY
694	\N	Pawnee County (County)	COUNTY
695	\N	Surry County (County)	COUNTY
696	\N	Lamoille County (County)	COUNTY
697	\N	Norton city (County)	COUNTY
698	\N	Taos County (County)	COUNTY
699	\N	Stephenson County (County)	COUNTY
700	\N	Lafayette County (County)	COUNTY
701	\N	Jutiapa (State / Territory)	STATE
702	\N	Tennessee (State / Territory)	STATE
703	\N	Manassas city (County)	COUNTY
704	\N	New Hampshire (State / Territory)	STATE
705	\N	Ford County (County)	COUNTY
706	\N	Ziebach County (County)	COUNTY
707	\N	McPherson County (County)	COUNTY
708	\N	Etowah County (County)	COUNTY
709	\N	Leavenworth County (County)	COUNTY
710	\N	Blair County (County)	COUNTY
711	\N	Republic of El Salvador (Country)	COUNTRY
712	\N	Muskogee County (County)	COUNTY
713	\N	JM (ISO Country Code)	ISO_COUNTRY
714	\N	Upson County (County)	COUNTY
715	\N	Conway County (County)	COUNTY
716	\N	Emery County (County)	COUNTY
717	\N	Department of Guadeloupe (Country)	COUNTRY
718	\N	Camuy Municipio (County)	COUNTY
719	\N	Wythe County (County)	COUNTY
720	\N	Adams County (County)	COUNTY
721	\N	Bent County (County)	COUNTY
722	\N	North Carolina (State / Territory)	STATE
723	\N	Quiche (State / Territory)	STATE
724	\N	Canóvanas Municipio (County)	COUNTY
725	\N	Letcher County (County)	COUNTY
726	\N	Avoyelles Parish (County)	COUNTY
727	\N	Wise County (County)	COUNTY
728	\N	Reno County (County)	COUNTY
729	\N	Mississippi County (County)	COUNTY
730	\N	Hocking County (County)	COUNTY
731	\N	Leslie County (County)	COUNTY
732	\N	Bennett County (County)	COUNTY
733	\N	Millard County (County)	COUNTY
734	\N	Neshoba County (County)	COUNTY
735	\N	Queen Anne's County (County)	COUNTY
736	\N	Daniels County (County)	COUNTY
737	\N	Webster County (County)	COUNTY
738	\N	Jersey County (County)	COUNTY
739	\N	Holmes County (County)	COUNTY
740	\N	Tallahatchie County (County)	COUNTY
741	\N	Mille Lacs County (County)	COUNTY
742	\N	Clarendon County (County)	COUNTY
743	\N	Colon (State / Territory)	STATE
744	\N	Guantanamo (State / Territory)	STATE
745	\N	Shenandoah County (County)	COUNTY
746	\N	Mariposa County (County)	COUNTY
747	\N	Prince Edward County (County)	COUNTY
748	\N	Whitley County (County)	COUNTY
749	\N	Missaukee County (County)	COUNTY
750	\N	Muhlenberg County (County)	COUNTY
751	\N	Buchanan County (County)	COUNTY
752	\N	Emporia city (County)	COUNTY
753	\N	Cortland County (County)	COUNTY
754	\N	Cayman Islands (Country)	COUNTRY
755	\N	Lamar County (County)	COUNTY
756	\N	Suffolk County (County)	COUNTY
757	\N	Twiggs County (County)	COUNTY
758	\N	Petersburg city (County)	COUNTY
759	\N	Glenn County (County)	COUNTY
760	\N	Alcona County (County)	COUNTY
761	\N	Sherburne County (County)	COUNTY
762	\N	Yucatan (State / Territory)	STATE
763	\N	Forrest County (County)	COUNTY
764	\N	Valley County (County)	COUNTY
765	\N	Herkimer County (County)	COUNTY
766	\N	Kenton County (County)	COUNTY
767	\N	St. Thomas Island (County)	COUNTY
768	\N	Victoria County (County)	COUNTY
769	\N	Bristol city (County)	COUNTY
770	\N	Galax city (County)	COUNTY
771	\N	Pushmataha County (County)	COUNTY
772	\N	Sacramento County (County)	COUNTY
773	\N	Renville County (County)	COUNTY
774	\N	Blaine County (County)	COUNTY
775	\N	Dauphin County (County)	COUNTY
776	\N	St. Lawrence County (County)	COUNTY
777	\N	Manatee County (County)	COUNTY
778	\N	Lake of the Woods County (County)	COUNTY
779	\N	Fond du Lac County (County)	COUNTY
780	\N	Bullitt County (County)	COUNTY
781	\N	Belknap County (County)	COUNTY
782	\N	Kearny County (County)	COUNTY
783	\N	Turner County (County)	COUNTY
784	\N	Montour County (County)	COUNTY
785	\N	DuPage County (County)	COUNTY
786	\N	Idaho (State / Territory)	STATE
787	\N	Woodford County (County)	COUNTY
788	\N	Custer County (County)	COUNTY
789	\N	Minnehaha County (County)	COUNTY
790	\N	Beaverhead County (County)	COUNTY
791	\N	Grundy County (County)	COUNTY
792	\N	Utah (State / Territory)	STATE
793	\N	Bee County (County)	COUNTY
794	\N	Jessamine County (County)	COUNTY
795	\N	Rio Blanco County (County)	COUNTY
796	\N	San Lorenzo Municipio (County)	COUNTY
797	\N	Hot Spring County (County)	COUNTY
798	\N	Screven County (County)	COUNTY
799	\N	Appling County (County)	COUNTY
800	\N	Dundy County (County)	COUNTY
801	\N	Ross County (County)	COUNTY
802	\N	Aguas Buenas Municipio (County)	COUNTY
803	\N	Hickory County (County)	COUNTY
804	\N	Atkinson County (County)	COUNTY
805	\N	Monterey County (County)	COUNTY
806	\N	Washtenaw County (County)	COUNTY
807	\N	Amelia County (County)	COUNTY
808	\N	Blue Earth County (County)	COUNTY
809	\N	Bates County (County)	COUNTY
810	\N	Dickinson County (County)	COUNTY
811	\N	Pine County (County)	COUNTY
812	\N	Yabucoa Municipio (County)	COUNTY
813	\N	Wabasha County (County)	COUNTY
814	\N	Chaffee County (County)	COUNTY
815	\N	Pasco County (County)	COUNTY
816	\N	Hardy County (County)	COUNTY
817	\N	Caldwell County (County)	COUNTY
818	\N	Trempealeau County (County)	COUNTY
819	\N	Mathews County (County)	COUNTY
820	\N	Bergen County (County)	COUNTY
821	\N	Loudoun County (County)	COUNTY
822	\N	Delaware County (County)	COUNTY
823	\N	Kenai Peninsula Borough (County)	COUNTY
824	\N	Appomattox County (County)	COUNTY
825	\N	Wakulla County (County)	COUNTY
826	\N	Cook County (County)	COUNTY
827	\N	Maine (State / Territory)	STATE
828	\N	Corson County (County)	COUNTY
829	\N	Cheshire County (County)	COUNTY
830	\N	Charles County (County)	COUNTY
831	\N	Chiapas (State / Territory)	STATE
832	\N	La Romana (State / Territory)	STATE
833	\N	Box Elder County (County)	COUNTY
834	\N	Douglas County (County)	COUNTY
835	\N	Hancock County (County)	COUNTY
836	\N	Grayson County (County)	COUNTY
837	\N	Nord-Est (State / Territory)	STATE
838	\N	Duarte (State / Territory)	STATE
839	\N	Matanzas (State / Territory)	STATE
840	\N	Hutchinson County (County)	COUNTY
841	\N	San Diego County (County)	COUNTY
842	\N	Pontotoc County (County)	COUNTY
843	\N	Ashley County (County)	COUNTY
844	\N	Greenland (Country)	COUNTRY
845	\N	British Columbia (State / Territory)	STATE
846	\N	Fairbanks North Star Borough (County)	COUNTY
847	\N	Benewah County (County)	COUNTY
848	\N	Hawkins County (County)	COUNTY
849	\N	San Pedro de Macor¡s (State / Territory)	STATE
850	\N	Page County (County)	COUNTY
851	\N	Antigua and Barbuda (Country)	COUNTRY
852	\N	Saint Andrew (State / Territory)	STATE
853	\N	Kankakee County (County)	COUNTY
854	\N	Carter County (County)	COUNTY
855	\N	Naguabo Municipio (County)	COUNTY
856	\N	Solano County (County)	COUNTY
857	\N	Furnas County (County)	COUNTY
858	\N	Doniphan County (County)	COUNTY
859	\N	Paulding County (County)	COUNTY
860	\N	Lancaster County (County)	COUNTY
861	\N	Island County (County)	COUNTY
862	\N	White Pine County (County)	COUNTY
863	\N	Hardin County (County)	COUNTY
864	\N	Turks and Caicos Islands (Country)	COUNTRY
865	\N	Tippecanoe County (County)	COUNTY
866	\N	Laclede County (County)	COUNTY
867	\N	Newton County (County)	COUNTY
868	\N	Churchill County (County)	COUNTY
869	\N	Barahona (State / Territory)	STATE
870	\N	Massac County (County)	COUNTY
871	\N	Russell County (County)	COUNTY
872	\N	Tripp County (County)	COUNTY
873	\N	Cavalier County (County)	COUNTY
874	\N	Sitka City and Borough (County)	COUNTY
875	\N	Lauderdale County (County)	COUNTY
876	\N	Payne County (County)	COUNTY
877	\N	Wasatch County (County)	COUNTY
878	\N	Barrow County (County)	COUNTY
879	\N	Nueces County (County)	COUNTY
880	\N	Francisco Morazan (State / Territory)	STATE
881	\N	McDonough County (County)	COUNTY
882	\N	Cheatham County (County)	COUNTY
883	\N	Audrain County (County)	COUNTY
884	\N	Stillwater County (County)	COUNTY
885	\N	Pembina County (County)	COUNTY
886	\N	Upshur County (County)	COUNTY
887	\N	Barber County (County)	COUNTY
888	\N	Stanton County (County)	COUNTY
889	\N	Bartholomew County (County)	COUNTY
890	\N	Hernando County (County)	COUNTY
891	\N	Chiquimula (State / Territory)	STATE
892	\N	Cortes (State / Territory)	STATE
893	\N	Bell County (County)	COUNTY
894	\N	Eau Claire County (County)	COUNTY
895	\N	Rock Island County (County)	COUNTY
896	\N	Boulder County (County)	COUNTY
897	\N	Mayes County (County)	COUNTY
898	\N	Montserrat (State / Territory)	STATE
899	\N	DO (ISO Country Code)	ISO_COUNTRY
900	\N	Benson County (County)	COUNTY
901	\N	Wyandot County (County)	COUNTY
902	\N	Lenoir County (County)	COUNTY
903	\N	Catahoula Parish (County)	COUNTY
904	\N	Harris County (County)	COUNTY
905	\N	Burt County (County)	COUNTY
906	\N	Brookings County (County)	COUNTY
907	\N	Kentucky (State / Territory)	STATE
908	\N	Grand Traverse County (County)	COUNTY
909	\N	Yamhill County (County)	COUNTY
910	\N	Gove County (County)	COUNTY
911	\N	Iredell County (County)	COUNTY
912	\N	Dade County (County)	COUNTY
913	\N	Pocahontas County (County)	COUNTY
914	\N	La Paz (State / Territory)	STATE
915	\N	Alamosa County (County)	COUNTY
916	\N	Transylvania County (County)	COUNTY
917	\N	Barnstable County (County)	COUNTY
918	\N	McCormick County (County)	COUNTY
919	\N	Nevada (State / Territory)	STATE
920	\N	Karnes County (County)	COUNTY
921	\N	Walker County (County)	COUNTY
922	\N	Trigg County (County)	COUNTY
923	\N	Baxter County (County)	COUNTY
924	\N	Fredericksburg city (County)	COUNTY
925	\N	Choluteca (State / Territory)	STATE
926	\N	Fulton County (County)	COUNTY
927	\N	Illinois (State / Territory)	STATE
928	\N	Ozaukee County (County)	COUNTY
929	\N	AG (ISO Country Code)	ISO_COUNTRY
930	\N	Salinas Municipio (County)	COUNTY
931	\N	Marengo County (County)	COUNTY
932	\N	Clackamas County (County)	COUNTY
933	\N	Bollinger County (County)	COUNTY
934	\N	Caswell County (County)	COUNTY
935	\N	Major County (County)	COUNTY
936	\N	Perkins County (County)	COUNTY
937	\N	Barry County (County)	COUNTY
938	\N	Rincón Municipio (County)	COUNTY
939	\N	Anguilla (State / Territory)	STATE
940	\N	Coryell County (County)	COUNTY
941	\N	Salem County (County)	COUNTY
942	\N	Bond County (County)	COUNTY
943	\N	Harrison County (County)	COUNTY
944	\N	Valdez-Cordova Census Area (County)	COUNTY
945	\N	Towner County (County)	COUNTY
946	\N	Androscoggin County (County)	COUNTY
947	\N	Boyle County (County)	COUNTY
948	\N	Cullman County (County)	COUNTY
949	\N	Sebastian County (County)	COUNTY
950	\N	Clark County (County)	COUNTY
951	\N	Storey County (County)	COUNTY
952	\N	Austin County (County)	COUNTY
953	\N	Carlton County (County)	COUNTY
954	\N	Baldwin County (County)	COUNTY
955	\N	Hertford County (County)	COUNTY
956	\N	Kane County (County)	COUNTY
957	\N	La Vega (State / Territory)	STATE
958	\N	Kewaunee County (County)	COUNTY
959	\N	Grainger County (County)	COUNTY
960	\N	Sampson County (County)	COUNTY
961	\N	Gage County (County)	COUNTY
962	\N	Kosciusko County (County)	COUNTY
963	\N	San Vicente (State / Territory)	STATE
964	\N	Cocke County (County)	COUNTY
965	\N	Wisconsin (State / Territory)	STATE
966	\N	Chambers County (County)	COUNTY
967	\N	Manistee County (County)	COUNTY
968	\N	Bowie County (County)	COUNTY
969	\N	York County (County)	COUNTY
970	\N	McLeod County (County)	COUNTY
971	\N	LaPorte County (County)	COUNTY
972	\N	LaMoure County (County)	COUNTY
973	\N	Talladega County (County)	COUNTY
974	\N	Espaillat (State / Territory)	STATE
975	\N	West Virginia (State / Territory)	STATE
976	\N	Autauga County (County)	COUNTY
977	\N	Chase County (County)	COUNTY
978	\N	Grand County (County)	COUNTY
979	\N	Matagorda County (County)	COUNTY
980	\N	Comayagua (State / Territory)	STATE
981	\N	Mercer County (County)	COUNTY
982	\N	Price County (County)	COUNTY
983	\N	Creek County (County)	COUNTY
984	\N	Spink County (County)	COUNTY
985	\N	Scotland County (County)	COUNTY
986	\N	Ogemaw County (County)	COUNTY
987	\N	Buffalo County (County)	COUNTY
988	\N	Uintah County (County)	COUNTY
989	\N	Scioto County (County)	COUNTY
990	\N	Cherry County (County)	COUNTY
991	\N	Larue County (County)	COUNTY
992	\N	St. Croix Island (County)	COUNTY
993	\N	Otsego County (County)	COUNTY
994	\N	Edgar County (County)	COUNTY
995	\N	Clearfield County (County)	COUNTY
996	\N	Bradford County (County)	COUNTY
997	\N	Florida Municipio (County)	COUNTY
998	\N	Eaton County (County)	COUNTY
999	\N	Travis County (County)	COUNTY
1000	\N	Hood County (County)	COUNTY
1001	\N	Fillmore County (County)	COUNTY
1002	\N	Chowan County (County)	COUNTY
1003	\N	Rockcastle County (County)	COUNTY
1004	\N	Grady County (County)	COUNTY
1005	\N	Lewis County (County)	COUNTY
1006	\N	San Cristobal (State / Territory)	STATE
1007	\N	Wilkinson County (County)	COUNTY
1008	\N	Sarasota County (County)	COUNTY
1009	\N	Ponce Municipio (County)	COUNTY
1010	\N	Granite County (County)	COUNTY
1011	\N	Flathead County (County)	COUNTY
1012	\N	Gasconade County (County)	COUNTY
1013	\N	Atoka County (County)	COUNTY
1014	\N	Moca Municipio (County)	COUNTY
1015	\N	Manatí Municipio (County)	COUNTY
1016	\N	Walton County (County)	COUNTY
1017	\N	Story County (County)	COUNTY
1018	\N	Izard County (County)	COUNTY
1019	\N	Lemhi County (County)	COUNTY
1020	\N	Ness County (County)	COUNTY
1021	\N	Genesee County (County)	COUNTY
1022	\N	Schoolcraft County (County)	COUNTY
1023	\N	Gilchrist County (County)	COUNTY
1024	\N	Bermuda (Country)	COUNTRY
1025	\N	Lehigh County (County)	COUNTY
1026	\N	Winona County (County)	COUNTY
1027	\N	Nord (State / Territory)	STATE
1028	\N	Huerfano County (County)	COUNTY
1029	\N	Ralls County (County)	COUNTY
1030	\N	SV (ISO Country Code)	ISO_COUNTRY
1031	\N	McKean County (County)	COUNTY
1032	\N	Lavaca County (County)	COUNTY
1033	\N	Utuado Municipio (County)	COUNTY
1034	\N	Chester County (County)	COUNTY
1035	\N	Mecosta County (County)	COUNTY
1036	\N	Peach County (County)	COUNTY
1037	\N	Coos County (County)	COUNTY
1038	\N	Calvert County (County)	COUNTY
1039	\N	Cole County (County)	COUNTY
1040	\N	Merced County (County)	COUNTY
1041	\N	Kearney County (County)	COUNTY
1042	\N	Ballard County (County)	COUNTY
1043	\N	Plaquemines Parish (County)	COUNTY
1044	\N	Ashe County (County)	COUNTY
1045	\N	Coweta County (County)	COUNTY
1046	\N	Iberia Parish (County)	COUNTY
1047	\N	Decatur County (County)	COUNTY
1048	\N	Silver Bow County (County)	COUNTY
1049	\N	Nottoway County (County)	COUNTY
1050	\N	Clay County (County)	COUNTY
1051	\N	Grimes County (County)	COUNTY
1052	\N	Red River County (County)	COUNTY
1053	\N	Itawamba County (County)	COUNTY
1054	\N	Horry County (County)	COUNTY
1055	\N	Venango County (County)	COUNTY
1056	\N	Lapeer County (County)	COUNTY
1057	\N	Belize (Country)	COUNTRY
1058	\N	Oscoda County (County)	COUNTY
1059	\N	Hopewell city (County)	COUNTY
1060	\N	Chisago County (County)	COUNTY
1061	\N	TC (ISO Country Code)	ISO_COUNTRY
1062	\N	Nobles County (County)	COUNTY
1063	\N	Canyon County (County)	COUNTY
1064	\N	Azua (State / Territory)	STATE
1065	\N	Long County (County)	COUNTY
1066	\N	Bayamon (State / Territory)	STATE
1067	\N	BZ (ISO Country Code)	ISO_COUNTRY
1068	\N	Candler County (County)	COUNTY
1069	\N	Richland Parish (County)	COUNTY
1070	\N	Yancey County (County)	COUNTY
1071	\N	GT (ISO Country Code)	ISO_COUNTRY
1072	\N	Oregon County (County)	COUNTY
1073	\N	Crook County (County)	COUNTY
1074	\N	Caddo Parish (County)	COUNTY
1075	\N	Morton County (County)	COUNTY
1076	\N	Blanco County (County)	COUNTY
1077	\N	Lexington city (County)	COUNTY
1078	\N	San Francisco County (County)	COUNTY
1079	\N	Covington County (County)	COUNTY
1080	\N	PR (ISO Country Code)	ISO_COUNTRY
1081	\N	Chatham County (County)	COUNTY
1082	\N	Wayne County (County)	COUNTY
1083	\N	Bayamón Municipio (County)	COUNTY
1084	\N	Irwin County (County)	COUNTY
1085	\N	BS (ISO Country Code)	ISO_COUNTRY
1086	\N	DM (ISO Country Code)	ISO_COUNTRY
1087	\N	Falls County (County)	COUNTY
1088	\N	Pacific County (County)	COUNTY
1089	\N	Nuckolls County (County)	COUNTY
1090	\N	Blount County (County)	COUNTY
1091	\N	Arkansas County (County)	COUNTY
1092	\N	Huntington County (County)	COUNTY
1093	\N	Monongalia County (County)	COUNTY
1094	\N	Amherst County (County)	COUNTY
1095	\N	Ouachita Parish (County)	COUNTY
1096	\N	Pamlico County (County)	COUNTY
1097	\N	Loíza Municipio (County)	COUNTY
1098	\N	East Feliciana Parish (County)	COUNTY
1099	\N	Crittenden County (County)	COUNTY
1100	\N	Spokane County (County)	COUNTY
1101	\N	Goodhue County (County)	COUNTY
1102	\N	Norman County (County)	COUNTY
1103	\N	Gracias a Dios (State / Territory)	STATE
1104	\N	Escuintla (State / Territory)	STATE
1105	\N	Anguilla (Country)	COUNTRY
1106	\N	Robeson County (County)	COUNTY
1107	\N	Weld County (County)	COUNTY
1108	\N	Monsenor Novel (State / Territory)	STATE
1109	\N	Burnet County (County)	COUNTY
1110	\N	Effingham County (County)	COUNTY
1111	\N	Chariton County (County)	COUNTY
1112	\N	Republic of Guatemala (Country)	COUNTRY
1113	\N	McDonald County (County)	COUNTY
1114	\N	Tipton County (County)	COUNTY
1115	\N	Albany County (County)	COUNTY
1116	\N	Florence County (County)	COUNTY
1117	\N	Greenville County (County)	COUNTY
1118	\N	Westchester County (County)	COUNTY
1119	\N	Ottawa County (County)	COUNTY
1120	\N	Bracken County (County)	COUNTY
1121	\N	Wibaux County (County)	COUNTY
1122	\N	Todd County (County)	COUNTY
1123	\N	St. Helena Parish (County)	COUNTY
1124	\N	Prince William County (County)	COUNTY
1125	\N	Hempstead County (County)	COUNTY
1126	\N	Nome Census Area (County)	COUNTY
1127	\N	Indiana (State / Territory)	STATE
1128	\N	Orleans County (County)	COUNTY
1129	\N	Brevard County (County)	COUNTY
1130	\N	Orange Walk (State / Territory)	STATE
1131	\N	Perquimans County (County)	COUNTY
1132	\N	Stone County (County)	COUNTY
1133	\N	Daviess County (County)	COUNTY
1134	\N	Michigan (State / Territory)	STATE
1135	\N	Pipestone County (County)	COUNTY
1136	\N	Clatsop County (County)	COUNTY
1137	\N	Greensville County (County)	COUNTY
1138	\N	Saluda County (County)	COUNTY
1139	\N	Stanislaus County (County)	COUNTY
1140	\N	Colonial Heights city (County)	COUNTY
1141	\N	Colleton County (County)	COUNTY
1142	\N	Puerto Plata (State / Territory)	STATE
1143	\N	Bonneville County (County)	COUNTY
1144	\N	St. Bernard Parish (County)	COUNTY
1145	\N	Pitt County (County)	COUNTY
1146	\N	Cheboygan County (County)	COUNTY
1147	\N	Brooks County (County)	COUNTY
1148	\N	Guthrie County (County)	COUNTY
1149	\N	Vilas County (County)	COUNTY
1150	\N	Shelby County (County)	COUNTY
1151	\N	Mendocino County (County)	COUNTY
1152	\N	Kanabec County (County)	COUNTY
1153	\N	Twin Falls County (County)	COUNTY
1154	\N	Marion County (County)	COUNTY
1155	\N	Hampton County (County)	COUNTY
1156	\N	Obion County (County)	COUNTY
1157	\N	Wabash County (County)	COUNTY
1158	\N	Mayagüez Municipio (County)	COUNTY
1159	\N	Sherman County (County)	COUNTY
1160	\N	Aiken County (County)	COUNTY
1161	\N	Oswego County (County)	COUNTY
1162	\N	Will County (County)	COUNTY
1163	\N	Benzie County (County)	COUNTY
1164	\N	Edmonson County (County)	COUNTY
1165	\N	Sanilac County (County)	COUNTY
1166	\N	Beaver County (County)	COUNTY
1167	\N	El Progreso (State / Territory)	STATE
1168	\N	Alachua County (County)	COUNTY
1169	\N	Waynesboro city (County)	COUNTY
1170	\N	Pottawattamie County (County)	COUNTY
1171	\N	Idaho County (County)	COUNTY
1172	\N	Los Angeles County (County)	COUNTY
1173	\N	New Brunswick (State / Territory)	STATE
1174	\N	Waldo County (County)	COUNTY
1175	\N	Tarrant County (County)	COUNTY
1176	\N	Alaska (State / Territory)	STATE
1177	\N	Nacogdoches County (County)	COUNTY
1178	\N	Dinwiddie County (County)	COUNTY
1179	\N	Geary County (County)	COUNTY
1180	\N	Henrico County (County)	COUNTY
1181	\N	Vestgronland (State / Territory)	STATE
1182	\N	Schuyler County (County)	COUNTY
1183	\N	Traverse County (County)	COUNTY
1184	\N	Rio Grande County (County)	COUNTY
1185	\N	Ritchie County (County)	COUNTY
1186	\N	Republic of Honduras (Country)	COUNTRY
1187	\N	Dawes County (County)	COUNTY
1188	\N	Hudson County (County)	COUNTY
1189	\N	Providence County (County)	COUNTY
1190	\N	Wapello County (County)	COUNTY
1191	\N	Miami County (County)	COUNTY
1192	\N	Spalding County (County)	COUNTY
1193	\N	Hubbard County (County)	COUNTY
1194	\N	Dane County (County)	COUNTY
1195	\N	Hamlin County (County)	COUNTY
1196	\N	Cape May County (County)	COUNTY
1197	\N	Crowley County (County)	COUNTY
1198	\N	Peravia (State / Territory)	STATE
1199	\N	Cumberland County (County)	COUNTY
1200	\N	Albemarle County (County)	COUNTY
1201	\N	Turks & Caicos Is. (State / Territory)	STATE
1202	\N	Sumter County (County)	COUNTY
1203	\N	Pratt County (County)	COUNTY
1204	\N	Okanogan County (County)	COUNTY
1205	\N	Cambria County (County)	COUNTY
1206	\N	Wyoming County (County)	COUNTY
1207	\N	Colorado County (County)	COUNTY
1208	\N	Dallas County (County)	COUNTY
1209	\N	Arecibo (State / Territory)	STATE
1210	\N	La Libertad (State / Territory)	STATE
1211	\N	Dent County (County)	COUNTY
1212	\N	Middlesex County (County)	COUNTY
1213	\N	Shannon County (County)	COUNTY
1214	\N	Covington city (County)	COUNTY
1215	\N	Charles City County (County)	COUNTY
1216	\N	Ahuachapan (State / Territory)	STATE
1217	\N	Dixie County (County)	COUNTY
1218	\N	Hampton city (County)	COUNTY
1219	\N	Usulutan (State / Territory)	STATE
1220	\N	Bryan County (County)	COUNTY
1221	\N	King and Queen County (County)	COUNTY
1222	\N	Lac qui Parle County (County)	COUNTY
1223	\N	Sweetwater County (County)	COUNTY
1224	\N	Arapahoe County (County)	COUNTY
1225	\N	Green County (County)	COUNTY
1226	\N	Lumpkin County (County)	COUNTY
1227	\N	Fort Bend County (County)	COUNTY
1228	\N	Coles County (County)	COUNTY
1229	\N	Judith Basin County (County)	COUNTY
1230	\N	Huron County (County)	COUNTY
1231	\N	Philadelphia County (County)	COUNTY
1232	\N	Walla Walla County (County)	COUNTY
1233	\N	Nelson County (County)	COUNTY
1234	\N	Orleans Parish (County)	COUNTY
1235	\N	Ellis County (County)	COUNTY
1236	\N	Woodson County (County)	COUNTY
1237	\N	Imperial County (County)	COUNTY
1238	\N	Powhatan County (County)	COUNTY
1239	\N	Monte Plata (State / Territory)	STATE
1240	\N	Delta County (County)	COUNTY
1241	\N	Gaston County (County)	COUNTY
1242	\N	Columbiana County (County)	COUNTY
1243	\N	Petersburg Census Area (County)	COUNTY
1244	\N	Newberry County (County)	COUNTY
1245	\N	Jim Wells County (County)	COUNTY
1246	\N	Centre (State / Territory)	STATE
1247	\N	Huntingdon County (County)	COUNTY
1248	\N	Bulloch County (County)	COUNTY
1249	\N	Winchester city (County)	COUNTY
1250	\N	Carteret County (County)	COUNTY
1251	\N	Kennebec County (County)	COUNTY
1252	\N	Fremont County (County)	COUNTY
1253	\N	Ontonagon County (County)	COUNTY
1254	\N	Yuba County (County)	COUNTY
1255	\N	Santiago Rodrigu (State / Territory)	STATE
1256	\N	Schoharie County (County)	COUNTY
1257	\N	McIntosh County (County)	COUNTY
1258	\N	Carson City (County)	COUNTY
1259	\N	Buckingham County (County)	COUNTY
1260	\N	Allen County (County)	COUNTY
1261	\N	Chickasaw County (County)	COUNTY
1262	\N	St. Mary's County (County)	COUNTY
1263	\N	Vanderburgh County (County)	COUNTY
1264	\N	McCurtain County (County)	COUNTY
1265	\N	Dewey County (County)	COUNTY
1266	\N	Camp County (County)	COUNTY
1267	\N	Davis County (County)	COUNTY
1268	\N	Abbeville County (County)	COUNTY
1269	\N	Elliott County (County)	COUNTY
1270	\N	Van Wert County (County)	COUNTY
1271	\N	Hunterdon County (County)	COUNTY
1272	\N	Polk County (County)	COUNTY
1273	\N	Waller County (County)	COUNTY
1274	\N	Sharp County (County)	COUNTY
1275	\N	Pennington County (County)	COUNTY
1276	\N	Hanover (State / Territory)	STATE
1277	\N	Durham County (County)	COUNTY
1278	\N	Clear Creek County (County)	COUNTY
1279	\N	Woods County (County)	COUNTY
1280	\N	Beltrami County (County)	COUNTY
1281	\N	Houghton County (County)	COUNTY
1282	\N	Sheridan County (County)	COUNTY
1283	\N	Crisp County (County)	COUNTY
1284	\N	Watauga County (County)	COUNTY
1285	\N	Concordia Parish (County)	COUNTY
1286	\N	Burnett County (County)	COUNTY
1287	\N	Walthall County (County)	COUNTY
1288	\N	Republic of Haiti (Country)	COUNTRY
1289	\N	Teller County (County)	COUNTY
1290	\N	Villa Clara (State / Territory)	STATE
1291	\N	Carolina Municipio (County)	COUNTY
1292	\N	Coshocton County (County)	COUNTY
1293	\N	Anoka County (County)	COUNTY
1294	\N	Tippah County (County)	COUNTY
1295	\N	Benton County (County)	COUNTY
1296	\N	Wallowa County (County)	COUNTY
1297	\N	Mesa County (County)	COUNTY
1298	\N	HT (ISO Country Code)	ISO_COUNTRY
1299	\N	Tishomingo County (County)	COUNTY
1300	\N	Yalobusha County (County)	COUNTY
1301	\N	Oglethorpe County (County)	COUNTY
1302	\N	Sully County (County)	COUNTY
1303	\N	Gibson County (County)	COUNTY
1304	\N	Clermont County (County)	COUNTY
1305	\N	San Marcos (State / Territory)	STATE
1306	\N	Calcasieu Parish (County)	COUNTY
1307	\N	Bastrop County (County)	COUNTY
1308	\N	Juneau County (County)	COUNTY
1309	\N	Osceola County (County)	COUNTY
1310	\N	Cienfuegos (State / Territory)	STATE
1311	\N	Berkshire County (County)	COUNTY
1312	\N	Trujillo Alto Municipio (County)	COUNTY
1313	\N	Washington (State / Territory)	STATE
1314	\N	Fayette County (County)	COUNTY
1315	\N	Terrebonne Parish (County)	COUNTY
1316	\N	Alpena County (County)	COUNTY
1317	\N	Carlisle County (County)	COUNTY
1318	\N	St. Francois County (County)	COUNTY
1319	\N	Gurabo Municipio (County)	COUNTY
1320	\N	Latah County (County)	COUNTY
1321	\N	Andrew County (County)	COUNTY
1322	\N	Roanoke County (County)	COUNTY
1323	\N	Santo Domingo (State / Territory)	STATE
1324	\N	Gloucester County (County)	COUNTY
1325	\N	Rappahannock County (County)	COUNTY
1326	\N	Choctaw County (County)	COUNTY
1327	\N	Northwest Arctic Borough (County)	COUNTY
1328	\N	Alpine County (County)	COUNTY
1329	\N	Humboldt County (County)	COUNTY
1330	\N	Lewis and Clark County (County)	COUNTY
1331	\N	Rooks County (County)	COUNTY
1332	\N	St. Lucia (Country)	COUNTRY
1333	\N	Dajabon (State / Territory)	STATE
1334	\N	Barron County (County)	COUNTY
1335	\N	Kandiyohi County (County)	COUNTY
1336	\N	Kingsbury County (County)	COUNTY
1337	\N	Perry County (County)	COUNTY
1338	\N	Miami-Dade County (County)	COUNTY
1339	\N	Hill County (County)	COUNTY
1340	\N	Hartley County (County)	COUNTY
1341	\N	O'Brien County (County)	COUNTY
1342	\N	Ramsey County (County)	COUNTY
1343	\N	Caledonia County (County)	COUNTY
1344	\N	Skagway Municipality (County)	COUNTY
1345	\N	Vernon County (County)	COUNTY
1346	\N	Vinton County (County)	COUNTY
1347	\N	Geauga County (County)	COUNTY
1348	\N	Fairfax city (County)	COUNTY
1349	\N	Tolland County (County)	COUNTY
1350	\N	Allen Parish (County)	COUNTY
1351	\N	Wilkin County (County)	COUNTY
1352	\N	Mahoning County (County)	COUNTY
1353	\N	Clare County (County)	COUNTY
1354	\N	Racine County (County)	COUNTY
1355	\N	Malheur County (County)	COUNTY
1356	\N	Keweenaw County (County)	COUNTY
1357	\N	Calloway County (County)	COUNTY
1358	\N	Atchison County (County)	COUNTY
1359	\N	Saskatchewan (State / Territory)	STATE
1360	\N	Quebec (State / Territory)	STATE
1361	\N	Chemung County (County)	COUNTY
1362	\N	Finney County (County)	COUNTY
1363	\N	Iron County (County)	COUNTY
1364	\N	Ashtabula County (County)	COUNTY
1365	\N	Morazan (State / Territory)	STATE
1366	\N	Baja California (State / Territory)	STATE
1367	\N	New Jersey (State / Territory)	STATE
1368	\N	Windham County (County)	COUNTY
1369	\N	Sauk County (County)	COUNTY
1370	\N	Marshall County (County)	COUNTY
1371	\N	Ouest (State / Territory)	STATE
1372	\N	Burleigh County (County)	COUNTY
1373	\N	Black Hawk County (County)	COUNTY
1374	\N	Dubuque County (County)	COUNTY
1375	\N	Miner County (County)	COUNTY
1376	\N	Harlan County (County)	COUNTY
1377	\N	Thomas County (County)	COUNTY
1378	\N	CA (ISO Country Code)	ISO_COUNTRY
1379	\N	Henry County (County)	COUNTY
1380	\N	Canadian County (County)	COUNTY
1381	\N	Fleming County (County)	COUNTY
1382	\N	Minidoka County (County)	COUNTY
1383	\N	Bosque County (County)	COUNTY
1384	\N	Tuscola County (County)	COUNTY
1385	\N	Las Piedras Municipio (County)	COUNTY
1386	\N	Glades County (County)	COUNTY
1387	\N	Baoruco (State / Territory)	STATE
1388	\N	Sanders County (County)	COUNTY
1389	\N	Las Tunas (State / Territory)	STATE
1390	\N	Piute County (County)	COUNTY
1391	\N	Ray County (County)	COUNTY
1392	\N	Napa County (County)	COUNTY
1393	\N	Caguas Municipio (County)	COUNTY
1394	\N	Ransom County (County)	COUNTY
1395	\N	Eagle County (County)	COUNTY
1396	\N	Licking County (County)	COUNTY
1397	\N	Simpson County (County)	COUNTY
1398	\N	San Miguel (State / Territory)	STATE
1399	\N	Pinellas County (County)	COUNTY
1400	\N	Goochland County (County)	COUNTY
1401	\N	Ida County (County)	COUNTY
1402	\N	Ferry County (County)	COUNTY
1403	\N	Halifax County (County)	COUNTY
1404	\N	Craighead County (County)	COUNTY
1405	\N	Lenawee County (County)	COUNTY
1406	\N	Crawford County (County)	COUNTY
1407	\N	Humacao (State / Territory)	STATE
1408	\N	Zelaya (State / Territory)	STATE
1409	\N	Pendleton County (County)	COUNTY
1410	\N	Saint Mary (State / Territory)	STATE
1411	\N	Darke County (County)	COUNTY
1412	\N	Lampasas County (County)	COUNTY
1413	\N	Cayo (State / Territory)	STATE
1414	\N	Panola County (County)	COUNTY
1415	\N	Kleberg County (County)	COUNTY
1416	\N	Prince of Wales-Hyder Census Area (County)	COUNTY
1417	\N	Wahkiakum County (County)	COUNTY
1418	\N	George County (County)	COUNTY
1419	\N	Sabana Grande Municipio (County)	COUNTY
1420	\N	Norton County (County)	COUNTY
1421	\N	Hormigueros Municipio (County)	COUNTY
1422	\N	Kaufman County (County)	COUNTY
1423	\N	Davison County (County)	COUNTY
1424	\N	Stewart County (County)	COUNTY
1425	\N	St. Kitts & Nevis (State / Territory)	STATE
1426	\N	Retalhuleu (State / Territory)	STATE
1427	\N	Kings County (County)	COUNTY
1428	\N	Garland County (County)	COUNTY
1429	\N	Moffat County (County)	COUNTY
1430	\N	Aguadilla Municipio (County)	COUNTY
1431	\N	Winneshiek County (County)	COUNTY
1432	\N	Neosho County (County)	COUNTY
1433	\N	Hemphill County (County)	COUNTY
1434	\N	Lafayette Parish (County)	COUNTY
1435	\N	Añasco Municipio (County)	COUNTY
1436	\N	Isanti County (County)	COUNTY
1437	\N	Gem County (County)	COUNTY
1438	\N	Dodge County (County)	COUNTY
1439	\N	Okfuskee County (County)	COUNTY
1440	\N	Wirt County (County)	COUNTY
1441	\N	Sangamon County (County)	COUNTY
1442	\N	Watonwan County (County)	COUNTY
1443	\N	Snyder County (County)	COUNTY
1444	\N	Washburn County (County)	COUNTY
1445	\N	DeKalb County (County)	COUNTY
1446	\N	McLean County (County)	COUNTY
1447	\N	Garrett County (County)	COUNTY
1448	\N	Bennington County (County)	COUNTY
1449	\N	Multnomah County (County)	COUNTY
1450	\N	Henderson County (County)	COUNTY
1451	\N	Montserrat (Country)	COUNTRY
1452	\N	Hamilton County (County)	COUNTY
1453	\N	Luquillo Municipio (County)	COUNTY
1454	\N	Territorial Collectivity of Saint Pierre (Country)	COUNTRY
1455	\N	Bartow County (County)	COUNTY
1456	\N	Dickey County (County)	COUNTY
1457	\N	Mellette County (County)	COUNTY
1458	\N	Iowa County (County)	COUNTY
1459	\N	Salt Lake County (County)	COUNTY
1460	\N	Manitoba (State / Territory)	STATE
1461	\N	Chelan County (County)	COUNTY
1462	\N	Kay County (County)	COUNTY
1463	\N	Noxubee County (County)	COUNTY
1464	\N	Martinique (State / Territory)	STATE
1465	\N	Oxford County (County)	COUNTY
1466	\N	Muscogee County (County)	COUNTY
1467	\N	Okaloosa County (County)	COUNTY
1468	\N	Metcalfe County (County)	COUNTY
1469	\N	Pemiscot County (County)	COUNTY
1470	\N	Pepin County (County)	COUNTY
1471	\N	Fauquier County (County)	COUNTY
1472	\N	BM (ISO Country Code)	ISO_COUNTRY
1473	\N	MS (ISO Country Code)	ISO_COUNTRY
1474	\N	Virginia (State / Territory)	STATE
1475	\N	East Carroll Parish (County)	COUNTY
1476	\N	Sunflower County (County)	COUNTY
1477	\N	Ravalli County (County)	COUNTY
1478	\N	Rockwall County (County)	COUNTY
1479	\N	Nebraska (State / Territory)	STATE
1480	\N	Quebradillas Municipio (County)	COUNTY
1481	\N	Montezuma County (County)	COUNTY
1482	\N	Kansas (State / Territory)	STATE
1483	\N	Lane County (County)	COUNTY
1484	\N	California (State / Territory)	STATE
1485	\N	Republic of Cuba (Country)	COUNTRY
1486	\N	Ogle County (County)	COUNTY
1487	\N	San Germán Municipio (County)	COUNTY
1488	\N	Hoonah-Angoon Census Area (County)	COUNTY
1489	\N	Dominica (State / Territory)	STATE
1490	\N	Las Marías Municipio (County)	COUNTY
1491	\N	Harnett County (County)	COUNTY
1492	\N	Wolfe County (County)	COUNTY
1493	\N	HN (ISO Country Code)	ISO_COUNTRY
1494	\N	Trousdale County (County)	COUNTY
1495	\N	Highlands County (County)	COUNTY
1496	\N	Citrus County (County)	COUNTY
1497	\N	Hampshire County (County)	COUNTY
1498	\N	Wabaunsee County (County)	COUNTY
1499	\N	Vermilion County (County)	COUNTY
1500	\N	Burleson County (County)	COUNTY
1501	\N	Waukesha County (County)	COUNTY
1502	\N	Moultrie County (County)	COUNTY
1503	\N	Chenango County (County)	COUNTY
1504	\N	Webster Parish (County)	COUNTY
1505	\N	Aransas County (County)	COUNTY
1506	\N	Langlade County (County)	COUNTY
1507	\N	Volusia County (County)	COUNTY
1508	\N	Davie County (County)	COUNTY
1509	\N	Brantley County (County)	COUNTY
1510	\N	Avery County (County)	COUNTY
1511	\N	Coahoma County (County)	COUNTY
1512	\N	Ingham County (County)	COUNTY
1513	\N	Berrien County (County)	COUNTY
1514	\N	Martinsville city (County)	COUNTY
1515	\N	VI (ISO Country Code)	ISO_COUNTRY
1516	\N	Portland (State / Territory)	STATE
1517	\N	Stevens County (County)	COUNTY
1518	\N	Bay County (County)	COUNTY
1519	\N	Rice County (County)	COUNTY
1520	\N	Williams County (County)	COUNTY
1521	\N	Cherokee County (County)	COUNTY
1522	\N	Cowlitz County (County)	COUNTY
1523	\N	St. Clair County (County)	COUNTY
1524	\N	Richland County (County)	COUNTY
1525	\N	Iowa (State / Territory)	STATE
1526	\N	Oktibbeha County (County)	COUNTY
1527	\N	South Carolina (State / Territory)	STATE
1528	\N	Atlantic County (County)	COUNTY
1529	\N	Gentry County (County)	COUNTY
1530	\N	St. John Island (County)	COUNTY
1531	\N	Grand Isle County (County)	COUNTY
1532	\N	Greenup County (County)	COUNTY
1533	\N	Morovis Municipio (County)	COUNTY
1534	\N	Olancho (State / Territory)	STATE
1535	\N	Faulk County (County)	COUNTY
1536	\N	Cottonwood County (County)	COUNTY
1537	\N	Waseca County (County)	COUNTY
1538	\N	Gilpin County (County)	COUNTY
1539	\N	Pike County (County)	COUNTY
1540	\N	Edmunds County (County)	COUNTY
1541	\N	Guánica Municipio (County)	COUNTY
1542	\N	Gadsden County (County)	COUNTY
1543	\N	Stutsman County (County)	COUNTY
1544	\N	Rock County (County)	COUNTY
1545	\N	Knott County (County)	COUNTY
1546	\N	Tuscarawas County (County)	COUNTY
1547	\N	Onondaga County (County)	COUNTY
1548	\N	Walsh County (County)	COUNTY
1549	\N	Niagara County (County)	COUNTY
1550	\N	Person County (County)	COUNTY
1551	\N	Teton County (County)	COUNTY
1552	\N	Jamaica (Country)	COUNTRY
1553	\N	Milwaukee County (County)	COUNTY
1554	\N	Amador County (County)	COUNTY
1555	\N	Morgan County (County)	COUNTY
1556	\N	McCook County (County)	COUNTY
1557	\N	Cannon County (County)	COUNTY
1558	\N	Drew County (County)	COUNTY
1559	\N	Switzerland County (County)	COUNTY
1560	\N	Falls Church city (County)	COUNTY
1561	\N	Putnam County (County)	COUNTY
1562	\N	Titus County (County)	COUNTY
1563	\N	Sullivan County (County)	COUNTY
1564	\N	Trumbull County (County)	COUNTY
1565	\N	La Habana (State / Territory)	STATE
1566	\N	Bremer County (County)	COUNTY
1567	\N	Morrison County (County)	COUNTY
1568	\N	Stoddard County (County)	COUNTY
1569	\N	Alabama (State / Territory)	STATE
1570	\N	Cheyenne County (County)	COUNTY
1571	\N	Macomb County (County)	COUNTY
1572	\N	St. Landry Parish (County)	COUNTY
1573	\N	Sheboygan County (County)	COUNTY
1574	\N	Aurora County (County)	COUNTY
1575	\N	Holguin (State / Territory)	STATE
1576	\N	New London County (County)	COUNTY
1577	\N	Antrim County (County)	COUNTY
1578	\N	Vermilion Parish (County)	COUNTY
1579	\N	Isle of Wight County (County)	COUNTY
1580	\N	St. Francis County (County)	COUNTY
1581	\N	Atlantida (State / Territory)	STATE
1582	\N	New Castle County (County)	COUNTY
1583	\N	Federation of Saint Kitts and Nevis (Country)	COUNTRY
1584	\N	Tunica County (County)	COUNTY
1585	\N	Attala County (County)	COUNTY
1586	\N	Accomack County (County)	COUNTY
1587	\N	Elkhart County (County)	COUNTY
1588	\N	Jack County (County)	COUNTY
1589	\N	Lempira (State / Territory)	STATE
1590	\N	Day County (County)	COUNTY
1591	\N	Lebanon County (County)	COUNTY
1592	\N	Yoro (State / Territory)	STATE
1593	\N	Prowers County (County)	COUNTY
1594	\N	Niobrara County (County)	COUNTY
1595	\N	Jasper County (County)	COUNTY
1596	\N	Deschutes County (County)	COUNTY
1597	\N	Orange County (County)	COUNTY
1598	\N	Madriz (State / Territory)	STATE
1599	\N	Otter Tail County (County)	COUNTY
1600	\N	District of Columbia (State / Territory)	STATE
1601	\N	Little River County (County)	COUNTY
1602	\N	Spencer County (County)	COUNTY
1603	\N	Monroe County (County)	COUNTY
1604	\N	Sutter County (County)	COUNTY
1605	\N	Richmond County (County)	COUNTY
1606	\N	Nassau County (County)	COUNTY
1607	\N	Dale County (County)	COUNTY
1608	\N	Luce County (County)	COUNTY
1609	\N	McKenzie County (County)	COUNTY
1610	\N	Cooper County (County)	COUNTY
1611	\N	New Kent County (County)	COUNTY
1612	\N	West Baton Rouge Parish (County)	COUNTY
1613	\N	Overton County (County)	COUNTY
1614	\N	Mecklenburg County (County)	COUNTY
1615	\N	North Slope Borough (County)	COUNTY
1616	\N	Dallam County (County)	COUNTY
1617	\N	Independencia (State / Territory)	STATE
1618	\N	Aitkin County (County)	COUNTY
1619	\N	Faribault County (County)	COUNTY
1620	\N	Comerío Municipio (County)	COUNTY
1621	\N	Edgefield County (County)	COUNTY
1622	\N	Fluvanna County (County)	COUNTY
1623	\N	Republic County (County)	COUNTY
1624	\N	Duplin County (County)	COUNTY
1625	\N	Hinds County (County)	COUNTY
1626	\N	Jo Daviess County (County)	COUNTY
1627	\N	Keith County (County)	COUNTY
1628	\N	Franklin Parish (County)	COUNTY
1629	\N	Trinity County (County)	COUNTY
1630	\N	Madison Parish (County)	COUNTY
1631	\N	Mountrail County (County)	COUNTY
1632	\N	Issaquena County (County)	COUNTY
1633	\N	Quitman County (County)	COUNTY
1634	\N	St. Tammany Parish (County)	COUNTY
1635	\N	Rolette County (County)	COUNTY
1636	\N	Klamath County (County)	COUNTY
1637	\N	Rhode Island (State / Territory)	STATE
1638	\N	Howell County (County)	COUNTY
1639	\N	Poinsett County (County)	COUNTY
1640	\N	Pulaski County (County)	COUNTY
1641	\N	Eureka County (County)	COUNTY
1642	\N	Poquoson city (County)	COUNTY
1643	\N	Norfolk County (County)	COUNTY
1644	\N	Galveston County (County)	COUNTY
1645	\N	Clearwater County (County)	COUNTY
1646	\N	Cascade County (County)	COUNTY
1647	\N	Laurel County (County)	COUNTY
1648	\N	Tyler County (County)	COUNTY
1649	\N	Dillon County (County)	COUNTY
1650	\N	Granma (State / Territory)	STATE
1651	\N	Dooly County (County)	COUNTY
1652	\N	Jerauld County (County)	COUNTY
1653	\N	Intibuca (State / Territory)	STATE
1654	\N	Harford County (County)	COUNTY
1655	\N	Wilson County (County)	COUNTY
1656	\N	Carroll County (County)	COUNTY
1657	\N	Chesapeake city (County)	COUNTY
1658	\N	Linn County (County)	COUNTY
1659	\N	Piatt County (County)	COUNTY
1660	\N	Fall River County (County)	COUNTY
1661	\N	Camden County (County)	COUNTY
1662	\N	Hamblen County (County)	COUNTY
1663	\N	Yauco Municipio (County)	COUNTY
1664	\N	Palo Pinto County (County)	COUNTY
1665	\N	Kalamazoo County (County)	COUNTY
1666	\N	Toa Alta Municipio (County)	COUNTY
1667	\N	Johnson County (County)	COUNTY
1668	\N	Hopkins County (County)	COUNTY
1669	\N	Carbon County (County)	COUNTY
1670	\N	Miller County (County)	COUNTY
1671	\N	Adair County (County)	COUNTY
1672	\N	Daggett County (County)	COUNTY
1673	\N	St. Joseph County (County)	COUNTY
1674	\N	Patillas Municipio (County)	COUNTY
1675	\N	Ashland County (County)	COUNTY
1676	\N	King William County (County)	COUNTY
1677	\N	Whatcom County (County)	COUNTY
1678	\N	Tyrrell County (County)	COUNTY
1679	\N	Corozal (State / Territory)	STATE
1680	\N	Ben Hill County (County)	COUNTY
1681	\N	Codington County (County)	COUNTY
1682	\N	Pueblo County (County)	COUNTY
1683	\N	District of Columbia (County)	COUNTY
1684	\N	Guayama (State / Territory)	STATE
1685	\N	Hoke County (County)	COUNTY
1686	\N	Wells County (County)	COUNTY
1687	\N	Cooke County (County)	COUNTY
1688	\N	Scott County (County)	COUNTY
1689	\N	Minnesota (State / Territory)	STATE
1690	\N	Alcorn County (County)	COUNTY
1691	\N	Gordon County (County)	COUNTY
1692	\N	Laramie County (County)	COUNTY
1693	\N	Ciudad de la Habana (State / Territory)	STATE
1694	\N	Gilmer County (County)	COUNTY
1695	\N	St. Johns County (County)	COUNTY
1696	\N	Breckinridge County (County)	COUNTY
1697	\N	Jenkins County (County)	COUNTY
1698	\N	Isabela Municipio (County)	COUNTY
1699	\N	Vernon Parish (County)	COUNTY
1700	\N	Chimaltenango (State / Territory)	STATE
1701	\N	Banks County (County)	COUNTY
1702	\N	Ceiba Municipio (County)	COUNTY
1703	\N	Cabarrus County (County)	COUNTY
1704	\N	San Juan County (County)	COUNTY
1705	\N	Sanborn County (County)	COUNTY
1706	\N	Hyde County (County)	COUNTY
1707	\N	Vega Alta Municipio (County)	COUNTY
1708	\N	Escambia County (County)	COUNTY
1709	\N	Lanier County (County)	COUNTY
1710	\N	Huehuetenango (State / Territory)	STATE
1711	\N	Bottineau County (County)	COUNTY
1712	\N	Meagher County (County)	COUNTY
1713	\N	Guantanamo Bay (State / Territory)	STATE
1714	\N	Aibonito Municipio (County)	COUNTY
1715	\N	Hanson County (County)	COUNTY
1716	\N	Trimble County (County)	COUNTY
1717	\N	Athens County (County)	COUNTY
1718	\N	Yakima County (County)	COUNTY
1719	\N	Grand Anse (State / Territory)	STATE
1720	\N	Kidder County (County)	COUNTY
1721	\N	Bronx County (County)	COUNTY
1722	\N	Becker County (County)	COUNTY
1723	\N	McMinn County (County)	COUNTY
1724	\N	Kingfisher County (County)	COUNTY
1725	\N	Mono County (County)	COUNTY
1726	\N	Ventura County (County)	COUNTY
1727	\N	Prairie County (County)	COUNTY
1728	\N	Powell County (County)	COUNTY
1729	\N	Campbell County (County)	COUNTY
1730	\N	King George County (County)	COUNTY
1731	\N	Hooker County (County)	COUNTY
1732	\N	Bolivar County (County)	COUNTY
1733	\N	Divide County (County)	COUNTY
1734	\N	Palo Alto County (County)	COUNTY
1735	\N	Christian County (County)	COUNTY
1736	\N	Camas County (County)	COUNTY
1737	\N	Conecuh County (County)	COUNTY
1738	\N	Granville County (County)	COUNTY
1739	\N	Treutlen County (County)	COUNTY
1740	\N	Treasure County (County)	COUNTY
1741	\N	St. Croix County (County)	COUNTY
1742	\N	LC (ISO Country Code)	ISO_COUNTRY
1743	\N	Weakley County (County)	COUNTY
1744	\N	Rusk County (County)	COUNTY
1745	\N	Rosebud County (County)	COUNTY
1746	\N	Reynolds County (County)	COUNTY
1747	\N	Outagamie County (County)	COUNTY
1748	\N	Meade County (County)	COUNTY
1749	\N	Montmorency County (County)	COUNTY
1750	\N	St. Lucie County (County)	COUNTY
1751	\N	Chicot County (County)	COUNTY
1752	\N	Cloud County (County)	COUNTY
1753	\N	Phelps County (County)	COUNTY
1754	\N	McLennan County (County)	COUNTY
1755	\N	Brazos County (County)	COUNTY
1756	\N	Lander County (County)	COUNTY
1757	\N	Cameron County (County)	COUNTY
1758	\N	Belize (State / Territory)	STATE
1759	\N	Guatemala (State / Territory)	STATE
1760	\N	Stann Creek (State / Territory)	STATE
1761	\N	Yakutat City and Borough (County)	COUNTY
1762	\N	Culpeper County (County)	COUNTY
1763	\N	Ciego de Avila (State / Territory)	STATE
1764	\N	LaGrange County (County)	COUNTY
1765	\N	Saguache County (County)	COUNTY
1766	\N	Schenectady County (County)	COUNTY
1767	\N	Iberville Parish (County)	COUNTY
1768	\N	Sumner County (County)	COUNTY
1769	\N	Conejos County (County)	COUNTY
1770	\N	Centre County (County)	COUNTY
1771	\N	Yates County (County)	COUNTY
1772	\N	Canada (Country)	COUNTRY
1773	\N	Lassen County (County)	COUNTY
1774	\N	El Dorado County (County)	COUNTY
1775	\N	Wicomico County (County)	COUNTY
1776	\N	Cecil County (County)	COUNTY
1777	\N	Freeborn County (County)	COUNTY
1778	\N	Pitkin County (County)	COUNTY
1779	\N	Guilford County (County)	COUNTY
1780	\N	Weston County (County)	COUNTY
1781	\N	Barceloneta Municipio (County)	COUNTY
1782	\N	Harvey County (County)	COUNTY
1783	\N	Manassas Park city (County)	COUNTY
2011	\N	Moore County (County)	COUNTY
1784	\N	Marquette County (County)	COUNTY
1785	\N	Pearl River County (County)	COUNTY
1786	\N	Owyhee County (County)	COUNTY
1787	\N	Preble County (County)	COUNTY
1788	\N	Shoshone County (County)	COUNTY
1789	\N	Somervell County (County)	COUNTY
1790	\N	Kodiak Island Borough (County)	COUNTY
1791	\N	Heard County (County)	COUNTY
1792	\N	Cidra Municipio (County)	COUNTY
1793	\N	Muskingum County (County)	COUNTY
1794	\N	Muskegon County (County)	COUNTY
1795	\N	Big Stone County (County)	COUNTY
1796	\N	Emanuel County (County)	COUNTY
1797	\N	Sublette County (County)	COUNTY
1798	\N	Echols County (County)	COUNTY
1799	\N	Marinette County (County)	COUNTY
1800	\N	Big Horn County (County)	COUNTY
1801	\N	Greene County (County)	COUNTY
1802	\N	Union County (County)	COUNTY
1803	\N	Houston County (County)	COUNTY
1804	\N	Barton County (County)	COUNTY
1805	\N	St. Louis city (County)	COUNTY
1806	\N	Vermont (State / Territory)	STATE
1807	\N	Commonwealth of The Bahamas (Country)	COUNTRY
1808	\N	Jefferson Parish (County)	COUNTY
1809	\N	Maria Trinidad S (State / Territory)	STATE
1810	\N	Caribou County (County)	COUNTY
1811	\N	Searcy County (County)	COUNTY
1812	\N	Hays County (County)	COUNTY
1813	\N	Sonsonate (State / Territory)	STATE
1814	\N	Saint Thomas (State / Territory)	STATE
1815	\N	Nicollet County (County)	COUNTY
1816	\N	Denton County (County)	COUNTY
1817	\N	Wadena County (County)	COUNTY
1818	\N	Barnes County (County)	COUNTY
1819	\N	Sharkey County (County)	COUNTY
1820	\N	Van Buren County (County)	COUNTY
1821	\N	Cuming County (County)	COUNTY
1822	\N	Salem city (County)	COUNTY
1823	\N	Mora County (County)	COUNTY
1824	\N	Muscatine County (County)	COUNTY
1825	\N	Arkansas (State / Territory)	STATE
1826	\N	Winston County (County)	COUNTY
1827	\N	Fresno County (County)	COUNTY
1828	\N	Staunton city (County)	COUNTY
1829	\N	Pettis County (County)	COUNTY
1830	\N	Columbia County (County)	COUNTY
1831	\N	Sanchez Ramirez (State / Territory)	STATE
1832	\N	Pedernales (State / Territory)	STATE
1833	\N	Norfolk city (County)	COUNTY
1834	\N	Wright County (County)	COUNTY
1835	\N	Trego County (County)	COUNTY
1836	\N	Stearns County (County)	COUNTY
1837	\N	Broadwater County (County)	COUNTY
1838	\N	Maunabo Municipio (County)	COUNTY
1839	\N	Charleston County (County)	COUNTY
1840	\N	Hartford County (County)	COUNTY
1841	\N	Strafford County (County)	COUNTY
1842	\N	Pierce County (County)	COUNTY
1843	\N	Guaynabo Municipio (County)	COUNTY
1844	\N	Wheatland County (County)	COUNTY
1845	\N	Roseau County (County)	COUNTY
1846	\N	Foster County (County)	COUNTY
1847	\N	Clinton County (County)	COUNTY
1848	\N	Marin County (County)	COUNTY
1849	\N	Oklahoma County (County)	COUNTY
1850	\N	Sioux County (County)	COUNTY
1851	\N	Sibley County (County)	COUNTY
1852	\N	Rensselaer County (County)	COUNTY
1853	\N	San Patricio County (County)	COUNTY
1854	\N	Cobb County (County)	COUNTY
1855	\N	El Seibo (State / Territory)	STATE
1856	\N	Pickens County (County)	COUNTY
1857	\N	Erath County (County)	COUNTY
1858	\N	Baca County (County)	COUNTY
1859	\N	Antigua & Barbuda (State / Territory)	STATE
1860	\N	Tattnall County (County)	COUNTY
1861	\N	Poweshiek County (County)	COUNTY
1862	\N	Oakland County (County)	COUNTY
1863	\N	Montgomery County (County)	COUNTY
1864	\N	Quintana Roo (State / Territory)	STATE
1865	\N	Currituck County (County)	COUNTY
1866	\N	Gladwin County (County)	COUNTY
1867	\N	Roane County (County)	COUNTY
1868	\N	Door County (County)	COUNTY
1869	\N	Brazoria County (County)	COUNTY
1870	\N	Allegany County (County)	COUNTY
1871	\N	Grays Harbor County (County)	COUNTY
1872	\N	Brunswick County (County)	COUNTY
1873	\N	Radford city (County)	COUNTY
1874	\N	Anderson County (County)	COUNTY
1875	\N	Ochiltree County (County)	COUNTY
1876	\N	Belmont County (County)	COUNTY
1877	\N	Green Lake County (County)	COUNTY
1878	\N	Toa Baja Municipio (County)	COUNTY
1879	\N	Okeechobee County (County)	COUNTY
1880	\N	Clallam County (County)	COUNTY
1881	\N	Dakota County (County)	COUNTY
1882	\N	Nicholas County (County)	COUNTY
1883	\N	Schuylkill County (County)	COUNTY
1884	\N	Modoc County (County)	COUNTY
1885	\N	Shawano County (County)	COUNTY
1886	\N	Asotin County (County)	COUNTY
1887	\N	Oconto County (County)	COUNTY
1888	\N	Colbert County (County)	COUNTY
1889	\N	Broome County (County)	COUNTY
1890	\N	Los Alamos County (County)	COUNTY
1891	\N	La Salle Parish (County)	COUNTY
1892	\N	Grafton County (County)	COUNTY
1893	\N	Shiawassee County (County)	COUNTY
1894	\N	Valverde (State / Territory)	STATE
1895	\N	Crenshaw County (County)	COUNTY
1896	\N	Bourbon County (County)	COUNTY
1897	\N	McDuffie County (County)	COUNTY
1898	\N	Ellsworth County (County)	COUNTY
1899	\N	De Soto Parish (County)	COUNTY
1900	\N	Sud (State / Territory)	STATE
1901	\N	Musselshell County (County)	COUNTY
1902	\N	Butte County (County)	COUNTY
1903	\N	Sevier County (County)	COUNTY
1904	\N	Kauai County (County)	COUNTY
1905	\N	Bath County (County)	COUNTY
1906	\N	Prince George's County (County)	COUNTY
1907	\N	Charlevoix County (County)	COUNTY
1908	\N	Toledo (State / Territory)	STATE
1909	\N	Chittenden County (County)	COUNTY
1910	\N	Cerro Gordo County (County)	COUNTY
1911	\N	Campeche (State / Territory)	STATE
1912	\N	South Dakota (State / Territory)	STATE
1913	\N	Guayama Municipio (County)	COUNTY
1914	\N	Georgetown County (County)	COUNTY
1915	\N	Zacapa (State / Territory)	STATE
1916	\N	Labette County (County)	COUNTY
1917	\N	Acadia Parish (County)	COUNTY
1918	\N	Lorain County (County)	COUNTY
1919	\N	Santa Rosa (State / Territory)	STATE
1920	\N	Juniata County (County)	COUNTY
1921	\N	La Altagracia (State / Territory)	STATE
1922	\N	Villalba Municipio (County)	COUNTY
1923	\N	Arlington County (County)	COUNTY
1924	\N	Morrow County (County)	COUNTY
1925	\N	Evangeline Parish (County)	COUNTY
1926	\N	Power County (County)	COUNTY
1927	\N	Garfield County (County)	COUNTY
1928	\N	Bannock County (County)	COUNTY
1929	\N	Navarro County (County)	COUNTY
1930	\N	Macon County (County)	COUNTY
1931	\N	Alameda County (County)	COUNTY
1932	\N	Newaygo County (County)	COUNTY
1933	\N	Calaveras County (County)	COUNTY
1934	\N	Buena Vista County (County)	COUNTY
1935	\N	Ulster County (County)	COUNTY
1936	\N	New Madrid County (County)	COUNTY
1937	\N	Northwest Territories (State / Territory)	STATE
1938	\N	Waupaca County (County)	COUNTY
1939	\N	Hand County (County)	COUNTY
1940	\N	CU (ISO Country Code)	ISO_COUNTRY
1941	\N	Fairfield County (County)	COUNTY
1942	\N	Meeker County (County)	COUNTY
1943	\N	Chalatenango (State / Territory)	STATE
1944	\N	Ketchikan Gateway Borough (County)	COUNTY
1945	\N	Porter County (County)	COUNTY
1946	\N	Dare County (County)	COUNTY
1947	\N	Arroyo Municipio (County)	COUNTY
1948	\N	Menifee County (County)	COUNTY
1949	\N	GP (ISO Country Code)	ISO_COUNTRY
1950	\N	Dickenson County (County)	COUNTY
1951	\N	Live Oak County (County)	COUNTY
1952	\N	Cuyahoga County (County)	COUNTY
1953	\N	Bucks County (County)	COUNTY
1954	\N	Saline County (County)	COUNTY
1955	\N	Rockland County (County)	COUNTY
1956	\N	Placer County (County)	COUNTY
1957	\N	Sancti Spiritus (State / Territory)	STATE
1958	\N	Evans County (County)	COUNTY
1959	\N	Bladen County (County)	COUNTY
1960	\N	Chouteau County (County)	COUNTY
1961	\N	McCracken County (County)	COUNTY
1962	\N	Bullock County (County)	COUNTY
1963	\N	Copan (State / Territory)	STATE
1964	\N	Yellowstone County (County)	COUNTY
1965	\N	Gallatin County (County)	COUNTY
1966	\N	Willacy County (County)	COUNTY
1967	\N	DeWitt County (County)	COUNTY
1968	\N	Ponce (State / Territory)	STATE
1969	\N	Unicoi County (County)	COUNTY
1970	\N	Sabine County (County)	COUNTY
1971	\N	Rutherford County (County)	COUNTY
1972	\N	Haakon County (County)	COUNTY
1973	\N	Franklin city (County)	COUNTY
1974	\N	Roanoke city (County)	COUNTY
1975	\N	Coal County (County)	COUNTY
1976	\N	Levy County (County)	COUNTY
1977	\N	Amite County (County)	COUNTY
1978	\N	Newfoundland (State / Territory)	STATE
1979	\N	Whitfield County (County)	COUNTY
1980	\N	Hardee County (County)	COUNTY
1981	\N	Tulare County (County)	COUNTY
1982	\N	St. John the Baptist Parish (County)	COUNTY
1983	\N	Latimer County (County)	COUNTY
1984	\N	Park County (County)	COUNTY
1985	\N	Nance County (County)	COUNTY
1986	\N	Wharton County (County)	COUNTY
1987	\N	Orocovis Municipio (County)	COUNTY
1988	\N	Worth County (County)	COUNTY
1989	\N	Owsley County (County)	COUNTY
1990	\N	Hitchcock County (County)	COUNTY
1991	\N	Bahamas (State / Territory)	STATE
1992	\N	Santiago de Cuba (State / Territory)	STATE
1993	\N	Schley County (County)	COUNTY
1994	\N	Denver County (County)	COUNTY
1995	\N	Fajardo Municipio (County)	COUNTY
1996	\N	Prince Edward Island (State / Territory)	STATE
1997	\N	Ringgold County (County)	COUNTY
1998	\N	Koochiching County (County)	COUNTY
1999	\N	Santa Rosa County (County)	COUNTY
2000	\N	Harrisonburg city (County)	COUNTY
2001	\N	Burlington County (County)	COUNTY
2002	\N	Botetourt County (County)	COUNTY
2003	\N	Lonoke County (County)	COUNTY
2004	\N	Ontario (State / Territory)	STATE
2005	\N	San Bernardino County (County)	COUNTY
2006	\N	Mifflin County (County)	COUNTY
2007	\N	Solola (State / Territory)	STATE
2008	\N	Dunn County (County)	COUNTY
2009	\N	Kenosha County (County)	COUNTY
2010	\N	Mayag?ez (State / Territory)	STATE
2012	\N	Yellow Medicine County (County)	COUNTY
2013	\N	Griggs County (County)	COUNTY
2014	\N	Prentiss County (County)	COUNTY
2015	\N	Saint Elizabeth (State / Territory)	STATE
2016	\N	Rutland County (County)	COUNTY
2017	\N	Hunt County (County)	COUNTY
2018	\N	Isabella County (County)	COUNTY
2019	\N	Yazoo County (County)	COUNTY
2020	\N	Salcedo (State / Territory)	STATE
2021	\N	Río Grande Municipio (County)	COUNTY
2022	\N	Laurens County (County)	COUNTY
2023	\N	Madison County (County)	COUNTY
2024	\N	Woodbury County (County)	COUNTY
2025	\N	Bowman County (County)	COUNTY
2026	\N	Bedford city (County)	COUNTY
2027	\N	Hawaii (State / Territory)	STATE
2028	\N	Appanoose County (County)	COUNTY
2029	\N	Santiago (State / Territory)	STATE
2030	\N	Leelanau County (County)	COUNTY
2031	\N	Glacier County (County)	COUNTY
2032	\N	Peñuelas Municipio (County)	COUNTY
2033	\N	Arthur County (County)	COUNTY
2034	\N	Shawnee County (County)	COUNTY
2035	\N	De Witt County (County)	COUNTY
2036	\N	Cassia County (County)	COUNTY
2037	\N	Suchitepequez (State / Territory)	STATE
2038	\N	St. Lucia (State / Territory)	STATE
2039	\N	Blackford County (County)	COUNTY
2040	\N	Fairfax County (County)	COUNTY
2041	\N	Coffey County (County)	COUNTY
2042	\N	St. Mary Parish (County)	COUNTY
2043	\N	Pickaway County (County)	COUNTY
2044	\N	Hampden County (County)	COUNTY
2045	\N	Susquehanna County (County)	COUNTY
2046	\N	Carver County (County)	COUNTY
2047	\N	Yuma County (County)	COUNTY
2048	\N	Weber County (County)	COUNTY
2049	\N	Sonoma County (County)	COUNTY
2050	\N	Newport News city (County)	COUNTY
2051	\N	Saunders County (County)	COUNTY
2052	\N	Buena Vista city (County)	COUNTY
2053	\N	Graves County (County)	COUNTY
2054	\N	Presque Isle County (County)	COUNTY
2055	\N	Van Zandt County (County)	COUNTY
2056	\N	Commonwealth of Puerto Rico (Country)	COUNTRY
2057	\N	San Salvador (State / Territory)	STATE
2058	\N	Virginia Beach city (County)	COUNTY
2059	\N	Woodruff County (County)	COUNTY
2060	\N	Washington Parish (County)	COUNTY
2061	\N	Ocotepeque (State / Territory)	STATE
2062	\N	Oregon (State / Territory)	STATE
2063	\N	Kit Carson County (County)	COUNTY
2064	\N	Caldwell Parish (County)	COUNTY
2065	\N	Parker County (County)	COUNTY
2066	\N	Whitman County (County)	COUNTY
2067	\N	Portage County (County)	COUNTY
2068	\N	Greeley County (County)	COUNTY
2069	\N	Jinotega (State / Territory)	STATE
2070	\N	Marlboro County (County)	COUNTY
2071	\N	Oaxaca (State / Territory)	STATE
2072	\N	Rio Arriba County (County)	COUNTY
2073	\N	Deuel County (County)	COUNTY
2074	\N	Thayer County (County)	COUNTY
2075	\N	San Luis Obispo County (County)	COUNTY
2076	\N	Starke County (County)	COUNTY
2077	\N	DeSoto County (County)	COUNTY
2078	\N	Summit County (County)	COUNTY
2079	\N	Dorado Municipio (County)	COUNTY
2080	\N	St. Charles County (County)	COUNTY
2081	\N	Denali Borough (County)	COUNTY
2082	\N	Wade Hampton Census Area (County)	COUNTY
2083	\N	Freestone County (County)	COUNTY
2084	\N	Summers County (County)	COUNTY
2085	\N	Coffee County (County)	COUNTY
2086	\N	Wilkes County (County)	COUNTY
2087	\N	Iosco County (County)	COUNTY
2088	\N	New York County (County)	COUNTY
2089	\N	British Virgin Is. (State / Territory)	STATE
2090	\N	Nash County (County)	COUNTY
2091	\N	Charlottesville city (County)	COUNTY
2092	\N	Alexandria city (County)	COUNTY
2093	\N	Jerome County (County)	COUNTY
2094	\N	Sweet Grass County (County)	COUNTY
2095	\N	Caroline County (County)	COUNTY
2096	\N	Plymouth County (County)	COUNTY
2097	\N	Ripley County (County)	COUNTY
2098	\N	Red River Parish (County)	COUNTY
2099	\N	Skamania County (County)	COUNTY
2100	\N	Spotsylvania County (County)	COUNTY
2101	\N	Larimer County (County)	COUNTY
2102	\N	Luzerne County (County)	COUNTY
2103	\N	Colfax County (County)	COUNTY
2104	\N	Kingman County (County)	COUNTY
2105	\N	Claiborne Parish (County)	COUNTY
2106	\N	Kootenai County (County)	COUNTY
2107	\N	Columbus County (County)	COUNTY
2108	\N	Keokuk County (County)	COUNTY
2109	\N	Oceana County (County)	COUNTY
2110	\N	Haines Borough (County)	COUNTY
2111	\N	Sagadahoc County (County)	COUNTY
2112	\N	Lexington County (County)	COUNTY
2113	\N	Erie County (County)	COUNTY
2114	\N	Yankton County (County)	COUNTY
2115	\N	Bingham County (County)	COUNTY
2116	\N	Nowata County (County)	COUNTY
2117	\N	Swain County (County)	COUNTY
2118	\N	Cayuga County (County)	COUNTY
2119	\N	Ouachita County (County)	COUNTY
2120	\N	Ohio (State / Territory)	STATE
2121	\N	Louisiana (State / Territory)	STATE
2122	\N	Craig County (County)	COUNTY
2123	\N	Saint James (State / Territory)	STATE
2124	\N	Olmsted County (County)	COUNTY
2125	\N	Woodward County (County)	COUNTY
2126	\N	Waushara County (County)	COUNTY
2127	\N	Oconee County (County)	COUNTY
2128	\N	Winnebago County (County)	COUNTY
2129	\N	Rockbridge County (County)	COUNTY
2130	\N	Franklin County (County)	COUNTY
2131	\N	Itasca County (County)	COUNTY
2132	\N	Gregg County (County)	COUNTY
2133	\N	Grand Forks County (County)	COUNTY
2134	\N	Ware County (County)	COUNTY
2135	\N	Tompkins County (County)	COUNTY
2136	\N	Umatilla County (County)	COUNTY
2137	\N	Jefferson Davis Parish (County)	COUNTY
2138	\N	Cowley County (County)	COUNTY
2139	\N	Monte Cristi (State / Territory)	STATE
2140	\N	Frederick County (County)	COUNTY
2141	\N	Angelina County (County)	COUNTY
2142	\N	Stafford County (County)	COUNTY
2143	\N	Hanover County (County)	COUNTY
2144	\N	Leon County (County)	COUNTY
2145	\N	Danville city (County)	COUNTY
2146	\N	Platte County (County)	COUNTY
2147	\N	Berkeley County (County)	COUNTY
2148	\N	Wyandotte County (County)	COUNTY
2149	\N	Collin County (County)	COUNTY
2150	\N	Kingston (State / Territory)	STATE
2151	\N	Greenwood County (County)	COUNTY
2152	\N	Riverside County (County)	COUNTY
2153	\N	Catoosa County (County)	COUNTY
2154	\N	Gogebic County (County)	COUNTY
2155	\N	Yadkin County (County)	COUNTY
2156	\N	Piscataquis County (County)	COUNTY
2157	\N	Dillingham Census Area (County)	COUNTY
2158	\N	Wyoming (State / Territory)	STATE
2159	\N	San Juan (State / Territory)	STATE
2160	\N	Elmore County (County)	COUNTY
2161	\N	Snohomish County (County)	COUNTY
2162	\N	San Benito County (County)	COUNTY
2163	\N	Tabasco (State / Territory)	STATE
2164	\N	St. Martin Parish (County)	COUNTY
2165	\N	Vance County (County)	COUNTY
2166	\N	Dolores County (County)	COUNTY
2167	\N	Gilliam County (County)	COUNTY
2168	\N	Sargent County (County)	COUNTY
2169	\N	Liberty County (County)	COUNTY
2170	\N	Bleckley County (County)	COUNTY
2171	\N	Union Parish (County)	COUNTY
2172	\N	Bledsoe County (County)	COUNTY
2173	\N	Clarke County (County)	COUNTY
2174	\N	Clinch County (County)	COUNTY
2175	\N	Adjuntas Municipio (County)	COUNTY
2176	\N	Cleveland County (County)	COUNTY
2177	\N	Hughes County (County)	COUNTY
2178	\N	Lipscomb County (County)	COUNTY
2179	\N	Sac County (County)	COUNTY
2180	\N	Peoria County (County)	COUNTY
2181	\N	Riley County (County)	COUNTY
2182	\N	Kern County (County)	COUNTY
2183	\N	Pend Oreille County (County)	COUNTY
2184	\N	Maryland (State / Territory)	STATE
2185	\N	Quezaltenango (State / Territory)	STATE
2186	\N	Hinsdale County (County)	COUNTY
2187	\N	Smith County (County)	COUNTY
2188	\N	Rains County (County)	COUNTY
2189	\N	Vega Baja Municipio (County)	COUNTY
2190	\N	Yell County (County)	COUNTY
2191	\N	Wetzel County (County)	COUNTY
2192	\N	Santa Clara County (County)	COUNTY
2193	\N	Nodaway County (County)	COUNTY
2194	\N	Billings County (County)	COUNTY
2195	\N	Richmond city (County)	COUNTY
2196	\N	Dutchess County (County)	COUNTY
2197	\N	LaSalle County (County)	COUNTY
2198	\N	Fallon County (County)	COUNTY
2199	\N	Bear Lake County (County)	COUNTY
2200	\N	Seneca County (County)	COUNTY
2201	\N	Tuolumne County (County)	COUNTY
2202	\N	Hettinger County (County)	COUNTY
2203	\N	Arenac County (County)	COUNTY
2204	\N	Sussex County (County)	COUNTY
2205	\N	Hato Major (State / Territory)	STATE
2206	\N	Richardson County (County)	COUNTY
2207	\N	Sawyer County (County)	COUNTY
2208	\N	Yukon-Koyukuk Census Area (County)	COUNTY
2209	\N	Alger County (County)	COUNTY
2210	\N	Ada County (County)	COUNTY
2211	\N	McCone County (County)	COUNTY
2212	\N	Boundary County (County)	COUNTY
2213	\N	Aleutians East Borough (County)	COUNTY
2214	\N	Steele County (County)	COUNTY
2215	\N	Juncos Municipio (County)	COUNTY
2216	\N	San Joaquin County (County)	COUNTY
2217	\N	Mineral County (County)	COUNTY
2218	\N	Vermillion County (County)	COUNTY
2219	\N	Darlington County (County)	COUNTY
2220	\N	Hennepin County (County)	COUNTY
2221	\N	Suffolk city (County)	COUNTY
2222	\N	Pinar del Rio (State / Territory)	STATE
2223	\N	Hillsborough County (County)	COUNTY
2224	\N	White County (County)	COUNTY
2225	\N	Nueva Segovia (State / Territory)	STATE
2226	\N	Red Lake County (County)	COUNTY
2227	\N	Smyth County (County)	COUNTY
2228	\N	Refugio County (County)	COUNTY
2229	\N	Wrangell City and Borough (County)	COUNTY
2230	\N	Yolo County (County)	COUNTY
2459	\N	Valle (State / Territory)	STATE
2231	\N	Limestone County (County)	COUNTY
2232	\N	Glynn County (County)	COUNTY
2233	\N	Kitsap County (County)	COUNTY
2234	\N	Tooele County (County)	COUNTY
2235	\N	Commonwealth of Dominica (Country)	COUNTRY
2236	\N	Murray County (County)	COUNTY
2237	\N	Osage County (County)	COUNTY
2238	\N	PM (ISO Country Code)	ISO_COUNTRY
2239	\N	Noble County (County)	COUNTY
2240	\N	Onslow County (County)	COUNTY
2241	\N	Alamance County (County)	COUNTY
2242	\N	Charles Mix County (County)	COUNTY
2243	\N	Anne Arundel County (County)	COUNTY
2244	\N	Fannin County (County)	COUNTY
2245	\N	Natrona County (County)	COUNTY
2246	\N	Skagit County (County)	COUNTY
2247	\N	Cabell County (County)	COUNTY
2248	\N	Taliaferro County (County)	COUNTY
2249	\N	Stark County (County)	COUNTY
2250	\N	Golden Valley County (County)	COUNTY
2251	\N	Wagoner County (County)	COUNTY
2252	\N	Mahaska County (County)	COUNTY
2253	\N	Nantucket County (County)	COUNTY
2254	\N	Meriwether County (County)	COUNTY
2255	\N	Oneida County (County)	COUNTY
2256	\N	Patrick County (County)	COUNTY
2257	\N	Hillsdale County (County)	COUNTY
2258	\N	Pope County (County)	COUNTY
2259	\N	Kossuth County (County)	COUNTY
2260	\N	Desha County (County)	COUNTY
2261	\N	Gregory County (County)	COUNTY
2262	\N	Lafourche Parish (County)	COUNTY
2263	\N	Utah County (County)	COUNTY
2264	\N	Cass County (County)	COUNTY
2265	\N	Suwannee County (County)	COUNTY
2266	\N	Corozal Municipio (County)	COUNTY
2267	\N	Toole County (County)	COUNTY
2268	\N	Somerset County (County)	COUNTY
2269	\N	Prince George County (County)	COUNTY
2270	\N	Forsyth County (County)	COUNTY
2271	\N	Estill County (County)	COUNTY
2272	\N	Cabo Rojo Municipio (County)	COUNTY
2273	\N	Florida (State / Territory)	STATE
2274	\N	St. Louis County (County)	COUNTY
2275	\N	Leflore County (County)	COUNTY
2276	\N	Taney County (County)	COUNTY
2277	\N	McNairy County (County)	COUNTY
2278	\N	Hendricks County (County)	COUNTY
2279	\N	Northampton County (County)	COUNTY
2280	\N	McClain County (County)	COUNTY
2281	\N	Claiborne County (County)	COUNTY
2282	\N	Rich County (County)	COUNTY
2283	\N	Distrito Nacional (State / Territory)	STATE
2284	\N	Mower County (County)	COUNTY
2285	\N	Moniteau County (County)	COUNTY
2286	\N	Ocean County (County)	COUNTY
2287	\N	Sanpete County (County)	COUNTY
2288	\N	Pershing County (County)	COUNTY
2289	\N	Merrimack County (County)	COUNTY
2290	\N	Le Flore County (County)	COUNTY
2291	\N	Bristol Bay Borough (County)	COUNTY
2292	\N	Washakie County (County)	COUNTY
2293	\N	Lee County (County)	COUNTY
2294	\N	Mahnomen County (County)	COUNTY
2295	\N	Robertson County (County)	COUNTY
2296	\N	Nevada County (County)	COUNTY
2297	\N	Frontier County (County)	COUNTY
2298	\N	Elbert County (County)	COUNTY
2299	\N	West Feliciana Parish (County)	COUNTY
2300	\N	Montcalm County (County)	COUNTY
2301	\N	Southeast Fairbanks Census Area (County)	COUNTY
2302	\N	Allegan County (County)	COUNTY
2303	\N	New Haven County (County)	COUNTY
2304	\N	Wood County (County)	COUNTY
2305	\N	Bamberg County (County)	COUNTY
2306	\N	James City County (County)	COUNTY
2307	\N	La Union (State / Territory)	STATE
2308	\N	Emmons County (County)	COUNTY
2309	\N	Saint Ann (State / Territory)	STATE
2310	\N	Duchesne County (County)	COUNTY
2311	\N	Montana (State / Territory)	STATE
2312	\N	Republic of Nicaragua (Country)	COUNTRY
2313	\N	Copiah County (County)	COUNTY
2314	\N	Aleutians West Census Area (County)	COUNTY
2315	\N	Mackinac County (County)	COUNTY
2316	\N	Plumas County (County)	COUNTY
2317	\N	Keya Paha County (County)	COUNTY
2318	\N	Bienville Parish (County)	COUNTY
2319	\N	Rankin County (County)	COUNTY
2320	\N	Jalapa (State / Territory)	STATE
2321	\N	Cuscatlan (State / Territory)	STATE
2322	\N	Walworth County (County)	COUNTY
2323	\N	Emmet County (County)	COUNTY
2324	\N	Cache County (County)	COUNTY
2325	\N	Allegheny County (County)	COUNTY
2326	\N	Charlotte County (County)	COUNTY
2327	\N	Charlton County (County)	COUNTY
2328	\N	Coosa County (County)	COUNTY
2329	\N	Greenbrier County (County)	COUNTY
2330	\N	Tift County (County)	COUNTY
2331	\N	Nez Perce County (County)	COUNTY
2332	\N	Dixon County (County)	COUNTY
2333	\N	Audubon County (County)	COUNTY
2334	\N	Loudon County (County)	COUNTY
2335	\N	Barbour County (County)	COUNTY
2336	\N	Rockingham County (County)	COUNTY
2337	\N	Glascock County (County)	COUNTY
2338	\N	Dubois County (County)	COUNTY
2339	\N	Barnwell County (County)	COUNTY
2340	\N	North Dakota (State / Territory)	STATE
2341	\N	Sud-Est (State / Territory)	STATE
2342	\N	Davidson County (County)	COUNTY
2343	\N	San Mateo County (County)	COUNTY
2344	\N	Westmoreland (State / Territory)	STATE
2345	\N	Totonicapan (State / Territory)	STATE
2346	\N	Collier County (County)	COUNTY
2347	\N	Izabal (State / Territory)	STATE
2348	\N	San Juan Municipio (County)	COUNTY
2349	\N	McDowell County (County)	COUNTY
2350	\N	Telfair County (County)	COUNTY
2351	\N	Maui County (County)	COUNTY
2352	\N	Clayton County (County)	COUNTY
2353	\N	Macoupin County (County)	COUNTY
2354	\N	Alta Verapaz (State / Territory)	STATE
2355	\N	Tazewell County (County)	COUNTY
2356	\N	Pender County (County)	COUNTY
2357	\N	Naranjito Municipio (County)	COUNTY
2358	\N	Lake and Peninsula Borough (County)	COUNTY
2359	\N	Ozark County (County)	COUNTY
2360	\N	Assumption Parish (County)	COUNTY
2361	\N	Dearborn County (County)	COUNTY
2362	\N	Vieques Municipio (County)	COUNTY
2363	\N	Wake County (County)	COUNTY
2364	\N	Jennings County (County)	COUNTY
2365	\N	Menominee County (County)	COUNTY
2366	\N	Ouray County (County)	COUNTY
2367	\N	Oliver County (County)	COUNTY
2368	\N	Chattooga County (County)	COUNTY
2369	\N	Humphreys County (County)	COUNTY
2370	\N	Elk County (County)	COUNTY
2371	\N	Gwinnett County (County)	COUNTY
2372	\N	Rogers County (County)	COUNTY
2373	\N	Rush County (County)	COUNTY
2374	\N	Iroquois County (County)	COUNTY
2375	\N	Love County (County)	COUNTY
2376	\N	Santa Barbara County (County)	COUNTY
2377	\N	Slope County (County)	COUNTY
2378	\N	Berks County (County)	COUNTY
2379	\N	Jackson Parish (County)	COUNTY
2380	\N	Livingston County (County)	COUNTY
2381	\N	Broward County (County)	COUNTY
2382	\N	Steuben County (County)	COUNTY
2383	\N	Las Animas County (County)	COUNTY
2384	\N	Chilton County (County)	COUNTY
2385	\N	Payette County (County)	COUNTY
2386	\N	Isla de la Juventud (State / Territory)	STATE
2387	\N	Santa Ana (State / Territory)	STATE
2388	\N	Cameron Parish (County)	COUNTY
2389	\N	Beaufort County (County)	COUNTY
2390	\N	Craven County (County)	COUNTY
2391	\N	Contra Costa County (County)	COUNTY
2392	\N	Tillamook County (County)	COUNTY
2393	\N	Jayuya Municipio (County)	COUNTY
2394	\N	Bibb County (County)	COUNTY
2395	\N	La Plata County (County)	COUNTY
2396	\N	Nemaha County (County)	COUNTY
2397	\N	Cleburne County (County)	COUNTY
2398	\N	Esmeralda County (County)	COUNTY
2399	\N	Talbot County (County)	COUNTY
2400	\N	Doddridge County (County)	COUNTY
2401	\N	Ste. Genevieve County (County)	COUNTY
2402	\N	Holt County (County)	COUNTY
2403	\N	Bedford County (County)	COUNTY
2404	\N	Forest County (County)	COUNTY
2405	\N	Sarpy County (County)	COUNTY
2406	\N	Tensas Parish (County)	COUNTY
2407	\N	La Estrelleta (State / Territory)	STATE
2408	\N	Callaway County (County)	COUNTY
2409	\N	GL (ISO Country Code)	ISO_COUNTRY
2410	\N	New Hanover County (County)	COUNTY
2411	\N	Nye County (County)	COUNTY
2412	\N	Seminole County (County)	COUNTY
2413	\N	Chattahoochee County (County)	COUNTY
2414	\N	Broomfield County (County)	COUNTY
2415	\N	Red Willow County (County)	COUNTY
2416	\N	Hickman County (County)	COUNTY
2417	\N	Faulkner County (County)	COUNTY
2418	\N	Butts County (County)	COUNTY
2419	\N	AI (ISO Country Code)	ISO_COUNTRY
2420	\N	Hot Springs County (County)	COUNTY
2421	\N	McHenry County (County)	COUNTY
2422	\N	Kenedy County (County)	COUNTY
2423	\N	Towns County (County)	COUNTY
2424	\N	Powder River County (County)	COUNTY
2425	\N	Scotts Bluff County (County)	COUNTY
2426	\N	Rowan County (County)	COUNTY
2427	\N	Tuscaloosa County (County)	COUNTY
2428	\N	Preston County (County)	COUNTY
2429	\N	Aroostook County (County)	COUNTY
2430	\N	Owen County (County)	COUNTY
2431	\N	Edgecombe County (County)	COUNTY
2432	\N	Montrose County (County)	COUNTY
2433	\N	Gratiot County (County)	COUNTY
2434	\N	Milam County (County)	COUNTY
2435	\N	Hood River County (County)	COUNTY
2436	\N	Wexford County (County)	COUNTY
2437	\N	Calhoun County (County)	COUNTY
2438	\N	Rabun County (County)	COUNTY
2439	\N	Boyd County (County)	COUNTY
2440	\N	Tangipahoa Parish (County)	COUNTY
2441	\N	Brule County (County)	COUNTY
2442	\N	Rhea County (County)	COUNTY
2443	\N	British Virgin Islands (Country)	COUNTRY
2444	\N	Rapides Parish (County)	COUNTY
2445	\N	St. Pierre & Miquelon (State / Territory)	STATE
2446	\N	Nova Scotia (State / Territory)	STATE
2447	\N	Bradley County (County)	COUNTY
2448	\N	Archuleta County (County)	COUNTY
2449	\N	Burke County (County)	COUNTY
2450	\N	Aguadilla (State / Territory)	STATE
2451	\N	Dominican Republic (Country)	COUNTRY
2452	\N	Washington County (County)	COUNTY
2453	\N	Dunklin County (County)	COUNTY
2454	\N	Livingston Parish (County)	COUNTY
2455	\N	Bermuda (State / Territory)	STATE
2456	\N	Saginaw County (County)	COUNTY
2457	\N	Otoe County (County)	COUNTY
2458	\N	Chippewa County (County)	COUNTY
2460	\N	Gallia County (County)	COUNTY
2461	\N	Virgin Is. (State / Territory)	STATE
2462	\N	Osborne County (County)	COUNTY
2463	\N	Traill County (County)	COUNTY
2464	\N	Phillips County (County)	COUNTY
2465	\N	Ascension Parish (County)	COUNTY
2466	\N	La Paz County (County)	COUNTY
2467	\N	Lajas Municipio (County)	COUNTY
2468	\N	Newport County (County)	COUNTY
2469	\N	Randolph County (County)	COUNTY
2470	\N	San Jacinto County (County)	COUNTY
2471	\N	Hayes County (County)	COUNTY
2472	\N	Lake County (County)	COUNTY
2473	\N	Natchitoches Parish (County)	COUNTY
2474	\N	Fountain County (County)	COUNTY
2475	\N	Bacon County (County)	COUNTY
2476	\N	Beadle County (County)	COUNTY
2477	\N	Goshen County (County)	COUNTY
2478	\N	Indiana County (County)	COUNTY
2479	\N	Alfalfa County (County)	COUNTY
2480	\N	Winn Parish (County)	COUNTY
2481	\N	Santa Barbara (State / Territory)	STATE
2482	\N	Goliad County (County)	COUNTY
2483	\N	Wilcox County (County)	COUNTY
2484	\N	Williamsburg city (County)	COUNTY
2485	\N	Butler County (County)	COUNTY
2486	\N	Mingo County (County)	COUNTY
2487	\N	Baraga County (County)	COUNTY
2488	\N	Kemper County (County)	COUNTY
2489	\N	Trelawny (State / Territory)	STATE
2490	\N	Pennsylvania (State / Territory)	STATE
2491	\N	Williamson County (County)	COUNTY
2492	\N	Chautauqua County (County)	COUNTY
2493	\N	KN (ISO Country Code)	ISO_COUNTRY
2494	\N	Jefferson Davis County (County)	COUNTY
2495	\N	Westmoreland County (County)	COUNTY
2496	\N	Lynchburg city (County)	COUNTY
2497	\N	Box Butte County (County)	COUNTY
2498	\N	United States Virgin Islands (Country)	COUNTRY
2499	\N	Kittson County (County)	COUNTY
2500	\N	Barranquitas Municipio (County)	COUNTY
2501	\N	Sedgwick County (County)	COUNTY
2502	\N	Ionia County (County)	COUNTY
2503	\N	Cedar County (County)	COUNTY
2504	\N	Essex County (County)	COUNTY
2505	\N	Flagler County (County)	COUNTY
2506	\N	Fentress County (County)	COUNTY
2507	\N	Baltimore County (County)	COUNTY
2508	\N	Sabine Parish (County)	COUNTY
2509	\N	Shasta County (County)	COUNTY
2510	\N	Harper County (County)	COUNTY
2511	\N	VG (ISO Country Code)	ISO_COUNTRY
2512	\N	Gulf County (County)	COUNTY
2513	\N	BO (ISO Country Code)	ISO_COUNTRY
2514	\N	Republic of Bolivia (Country)	COUNTRY
2515	\N	Puno (State / Territory)	STATE
2516	\N	Republic of Peru (Country)	COUNTRY
2517	\N	PE (ISO Country Code)	ISO_COUNTRY
2518	\N	RU (ISO Country Code)	ISO_COUNTRY
2519	\N	Chukotskiy avtonomnyy okrug (State / Territory)	STATE
2520	\N	Russian Federation (Country)	COUNTRY
2521	\N	Hedmark (State / Territory)	STATE
2522	\N	SE (ISO Country Code)	ISO_COUNTRY
2523	\N	Vest-Agder (State / Territory)	STATE
2524	\N	Sogn Og Fjordane (State / Territory)	STATE
2525	\N	Ostfold (State / Territory)	STATE
2526	\N	Vestfold (State / Territory)	STATE
2527	\N	Aust-Agder (State / Territory)	STATE
2528	\N	Telemark (State / Territory)	STATE
2529	\N	Goteborgs Och Bohus (State / Territory)	STATE
2530	\N	Alvsborgs (State / Territory)	STATE
2531	\N	Kingdom of Sweden (Country)	COUNTRY
2532	\N	Kingdom of Norway (Country)	COUNTRY
2533	\N	Buskerud (State / Territory)	STATE
2534	\N	NO (ISO Country Code)	ISO_COUNTRY
2535	\N	Varmlands (State / Territory)	STATE
2536	\N	Oslo (State / Territory)	STATE
2537	\N	Oppland (State / Territory)	STATE
2538	\N	Akershus (State / Territory)	STATE
2539	\N	Ngamiland (State / Territory)	STATE
2540	\N	BW (ISO Country Code)	ISO_COUNTRY
2541	\N	Boesmanland (State / Territory)	STATE
2542	\N	Republic of Botswana (Country)	COUNTRY
2543	\N	Republic of Namibia (Country)	COUNTRY
2544	\N	NA (ISO Country Code)	ISO_COUNTRY
2545	\N	French Republic (Country)	COUNTRY
2546	\N	Basse-Normandie (State / Territory)	STATE
2547	\N	Picardie (State / Territory)	STATE
2548	\N	GB (ISO Country Code)	ISO_COUNTRY
2549	\N	Isle of Man (State / Territory)	STATE
2550	\N	England (State / Territory)	STATE
2551	\N	Nord-Pas-de-Calais (State / Territory)	STATE
2552	\N	FR (ISO Country Code)	ISO_COUNTRY
2553	\N	Wales (State / Territory)	STATE
2554	\N	United Kingdom of Great Britain and Nort (Country)	COUNTRY
2555	\N	Scotland (State / Territory)	STATE
2556	\N	Haute-Normandie (State / Territory)	STATE
2557	\N	Northern Ireland (State / Territory)	STATE
2558	\N	Ireland (Country)	COUNTRY
2559	\N	Ulster (State / Territory)	STATE
2560	\N	IE (ISO Country Code)	ISO_COUNTRY
2561	\N	Leinster (State / Territory)	STATE
2562	\N	Munster (State / Territory)	STATE
2563	\N	Isle of Man (Country)	COUNTRY
2564	\N	Connacht (State / Territory)	STATE
2565	\N	Hainaut (State / Territory)	STATE
2566	\N	Guernsey (State / Territory)	STATE
2567	\N	Kingdom of Belgium (Country)	COUNTRY
2568	\N	Bailiwick of Guernsey (Country)	COUNTRY
2569	\N	BE (ISO Country Code)	ISO_COUNTRY
2570	\N	West-Vlaanderen (State / Territory)	STATE
2571	\N	Nordgronland (State / Territory)	STATE
2572	\N	Vesturland (State / Territory)	STATE
2573	\N	Vestfirdhir (State / Territory)	STATE
2574	\N	Reykjavik (State / Territory)	STATE
2575	\N	Sudhurland (State / Territory)	STATE
2576	\N	Republic of Iceland (Country)	COUNTRY
2577	\N	Nordhurland Eystra (State / Territory)	STATE
2578	\N	IS (ISO Country Code)	ISO_COUNTRY
2579	\N	Austurland (State / Territory)	STATE
2580	\N	Nordhurland Vestra (State / Territory)	STATE
2581	\N	Gaziantep (State / Territory)	STATE
2582	\N	TR (ISO Country Code)	ISO_COUNTRY
2583	\N	Malatya (State / Territory)	STATE
2584	\N	Adana (State / Territory)	STATE
2585	\N	Adiyaman (State / Territory)	STATE
2586	\N	Republic of Turkey (Country)	COUNTRY
2587	\N	Kahraman Maras (State / Territory)	STATE
2588	\N	Ostgronland (State / Territory)	STATE
2589	\N	Nord-Trondelag (State / Territory)	STATE
2590	\N	Rogaland (State / Territory)	STATE
2591	\N	Faroe Islands (Country)	COUNTRY
2592	\N	Sor-Trondelag (State / Territory)	STATE
2593	\N	FO (ISO Country Code)	ISO_COUNTRY
2594	\N	Faeroe Islands (State / Territory)	STATE
2595	\N	Jamtlands (State / Territory)	STATE
2596	\N	Hordaland (State / Territory)	STATE
2597	\N	Nordland (State / Territory)	STATE
2598	\N	More Og Romsdal (State / Territory)	STATE
2599	\N	Chiriqui (State / Territory)	STATE
2600	\N	PA (ISO Country Code)	ISO_COUNTRY
2601	\N	Republic of Panama (Country)	COUNTRY
2602	\N	Bocas del Toro (State / Territory)	STATE
2603	\N	Rota Municipality (County)	COUNTY
2604	\N	Darien (State / Territory)	STATE
2605	\N	Panama (State / Territory)	STATE
2606	\N	Los Santos (State / Territory)	STATE
2607	\N	Choco (State / Territory)	STATE
2608	\N	Antioquia (State / Territory)	STATE
2609	\N	Veraguas (State / Territory)	STATE
2610	\N	Republic of Colombia (Country)	COUNTRY
2611	\N	San Blas (State / Territory)	STATE
2612	\N	Herrera (State / Territory)	STATE
2613	\N	CO (ISO Country Code)	ISO_COUNTRY
2614	\N	Cocle (State / Territory)	STATE
2615	\N	Republic of Costa Rica (Country)	COUNTRY
2616	\N	CR (ISO Country Code)	ISO_COUNTRY
2617	\N	Puntarenas (State / Territory)	STATE
2618	\N	Limon (State / Territory)	STATE
2619	\N	Caldas (State / Territory)	STATE
2620	\N	Huila (State / Territory)	STATE
2621	\N	Chontales (State / Territory)	STATE
2622	\N	Chinandega (State / Territory)	STATE
2623	\N	Valle del Cauca (State / Territory)	STATE
2624	\N	Tolima (State / Territory)	STATE
2625	\N	Guanacaste (State / Territory)	STATE
2626	\N	Sucre (State / Territory)	STATE
2627	\N	Matagalpa (State / Territory)	STATE
2628	\N	Cartago (State / Territory)	STATE
2629	\N	San Andres y Providencia (State / Territory)	STATE
2630	\N	Masaya (State / Territory)	STATE
2631	\N	Heredia (State / Territory)	STATE
2632	\N	Cordoba (State / Territory)	STATE
2633	\N	Bolivar (State / Territory)	STATE
2634	\N	Risaralda (State / Territory)	STATE
2635	\N	Quindio (State / Territory)	STATE
2636	\N	Alajuela (State / Territory)	STATE
2637	\N	Cauca (State / Territory)	STATE
2638	\N	Leon (State / Territory)	STATE
2639	\N	Boaco (State / Territory)	STATE
2640	\N	Carazo (State / Territory)	STATE
2641	\N	San Jose (State / Territory)	STATE
2642	\N	Atlantico (State / Territory)	STATE
2643	\N	Rivas (State / Territory)	STATE
2644	\N	Managua (State / Territory)	STATE
2645	\N	Granada (State / Territory)	STATE
2646	\N	Esteli (State / Territory)	STATE
2647	\N	Rio San Juan (State / Territory)	STATE
2648	\N	East Berbice-Corentyne (State / Territory)	STATE
2649	\N	Guainia (State / Territory)	STATE
2650	\N	TT (ISO Country Code)	ISO_COUNTRY
2651	\N	BR (ISO Country Code)	ISO_COUNTRY
2652	\N	Loja (State / Territory)	STATE
2653	\N	St. Vincent & the Grenadines (State / Territory)	STATE
2654	\N	Guarico (State / Territory)	STATE
2655	\N	Casanare (State / Territory)	STATE
2656	\N	Yaracuy (State / Territory)	STATE
2657	\N	Canar (State / Territory)	STATE
2658	\N	Neuva Esparta (State / Territory)	STATE
2659	\N	Aragua (State / Territory)	STATE
2660	\N	Para (State / Territory)	STATE
2661	\N	Portuguesa (State / Territory)	STATE
2662	\N	Guayas (State / Territory)	STATE
2663	\N	Miranda (State / Territory)	STATE
2664	\N	Imbabura (State / Territory)	STATE
2665	\N	Sucumbios (State / Territory)	STATE
2666	\N	Bolivarian Republic of Venezuela (Country)	COUNTRY
2667	\N	Piura (State / Territory)	STATE
2668	\N	Falcon (State / Territory)	STATE
2669	\N	Carchi (State / Territory)	STATE
2670	\N	SR (ISO Country Code)	ISO_COUNTRY
2671	\N	Republic of Trinidad and Tobago (Country)	COUNTRY
2672	\N	Tungurahua (State / Territory)	STATE
2673	\N	EC (ISO Country Code)	ISO_COUNTRY
2674	\N	Pastaza (State / Territory)	STATE
2675	\N	Sipaliwini (State / Territory)	STATE
2790	\N	Formative	\N
2676	\N	AW (ISO Country Code)	ISO_COUNTRY
2677	\N	Cesar (State / Territory)	STATE
2678	\N	Napo (State / Territory)	STATE
2679	\N	Barbados (Country)	COUNTRY
2680	\N	Monagas (State / Territory)	STATE
2681	\N	Litigated Zone (State / Territory)	STATE
2682	\N	Chimborazo (State / Territory)	STATE
2683	\N	Tumbes (State / Territory)	STATE
2684	\N	Upper Demerara-Berbice (State / Territory)	STATE
2685	\N	Essequibo Islands-West Demerara (State / Territory)	STATE
2686	\N	Cundinamarca (State / Territory)	STATE
2687	\N	Potaro-Siparuni (State / Territory)	STATE
2688	\N	Los Rios (State / Territory)	STATE
2689	\N	Merida (State / Territory)	STATE
2690	\N	VC (ISO Country Code)	ISO_COUNTRY
2691	\N	Grenada (State / Territory)	STATE
2692	\N	Amazonas (State / Territory)	STATE
2693	\N	Zamora-Chinchipe (State / Territory)	STATE
2694	\N	Anzoategui (State / Territory)	STATE
2695	\N	Dependencias Federales (State / Territory)	STATE
2696	\N	Lara (State / Territory)	STATE
2697	\N	Co-operative Republic of Guyana (Country)	COUNTRY
2698	\N	Netherlands Antilles (Country)	COUNTRY
2699	\N	Trujillo (State / Territory)	STATE
2700	\N	Trinidad & Tobago (State / Territory)	STATE
2701	\N	Netherlands Antilles (State / Territory)	STATE
2702	\N	Santander (State / Territory)	STATE
2703	\N	Meta (State / Territory)	STATE
2704	\N	GY (ISO Country Code)	ISO_COUNTRY
2705	\N	Grenada (Country)	COUNTRY
2706	\N	St. Vincent and the Grenadines (Country)	COUNTRY
2707	\N	Magdalena (State / Territory)	STATE
2708	\N	Cojedes (State / Territory)	STATE
2709	\N	Galapagos (State / Territory)	STATE
2710	\N	Upper Takutu-Upper Essequibo (State / Territory)	STATE
2711	\N	Republic of Suriname (Country)	COUNTRY
2712	\N	Zulia (State / Territory)	STATE
2713	\N	Norde de Santander (State / Territory)	STATE
2714	\N	Cotopaxi (State / Territory)	STATE
2715	\N	Coronie (State / Territory)	STATE
2716	\N	Delta Amacuro (State / Territory)	STATE
2717	\N	Distrito Especial (State / Territory)	STATE
2718	\N	Aruba (State / Territory)	STATE
2719	\N	Aruba (Country)	COUNTRY
2720	\N	Loreto (State / Territory)	STATE
2721	\N	Morona-Santiago (State / Territory)	STATE
2722	\N	El Oro (State / Territory)	STATE
2723	\N	Manabi (State / Territory)	STATE
2724	\N	AN (ISO Country Code)	ISO_COUNTRY
2725	\N	Barbados (State / Territory)	STATE
2726	\N	Republic of Ecuador (Country)	COUNTRY
2727	\N	Vaupes (State / Territory)	STATE
2728	\N	Mahaica-Berbice (State / Territory)	STATE
2729	\N	Esmeraldas (State / Territory)	STATE
2730	\N	Barinas (State / Territory)	STATE
2731	\N	Vichada (State / Territory)	STATE
2732	\N	BB (ISO Country Code)	ISO_COUNTRY
2733	\N	VE (ISO Country Code)	ISO_COUNTRY
2734	\N	Caqueta (State / Territory)	STATE
2735	\N	Demerara-Mahaica (State / Territory)	STATE
2736	\N	GD (ISO Country Code)	ISO_COUNTRY
2737	\N	Pomeroon-Supenaam (State / Territory)	STATE
2738	\N	Carabobo (State / Territory)	STATE
2739	\N	Tachira (State / Territory)	STATE
2740	\N	Boyaca (State / Territory)	STATE
2741	\N	Azuay (State / Territory)	STATE
2742	\N	Cuyuni-Mazaruni (State / Territory)	STATE
2743	\N	Guaviare (State / Territory)	STATE
2744	\N	Nickerie (State / Territory)	STATE
2745	\N	Narino (State / Territory)	STATE
2746	\N	Arauca (State / Territory)	STATE
2747	\N	Barima-Waini (State / Territory)	STATE
2748	\N	Roraima (State / Territory)	STATE
2749	\N	Federative Republic of Brazil (Country)	COUNTRY
2750	\N	Pichincha (State / Territory)	STATE
2751	\N	Putumayo (State / Territory)	STATE
2752	\N	La Guajira (State / Territory)	STATE
2753	\N	Apure (State / Territory)	STATE
2754	\N	Agua Fria	\N
2755	\N	JO (ISO Country Code)	ISO_COUNTRY
2756	\N	Ma'an (State / Territory)	STATE
2757	\N	Hashemite Kingdom of Jordan (Country)	COUNTRY
2758	\N	Tucson Basin	\N
2759	\N	Agua Fria drainage	\N
2760	\N	New River drainage	\N
2761	\N	east-cental Arizona	\N
2762	\N	Northern Sinagua	\N
2763	\N	Pueblo of Zuni	\N
2764	\N	Northern Rio Grande	\N
2765	\N	southwestern Colorado	\N
2766	\N	Animas	\N
2767	\N	La Plata	\N
2768	\N	Ridges Basin	\N
2769	\N	MImbres	\N
2770	\N	southwestern New Mexico	\N
2771	\N	Mimbres River	\N
2772	\N	Sonoran Desert	\N
2773	\N	northern Sonora	\N
2774	\N	southeastern Arizona	\N
2775	\N	Cienaga Creek	\N
2776	\N	Mammoth Cave	\N
2777	\N	Flagstaff	\N
2778	\N	Prescott	\N
2779	\N	Sunset Crater	\N
2780	\N	Deadman Wash	\N
2781	\N	Rio de Flag	\N
2782	\N	San Francisco Peaks	\N
2783	\N	Elden Mountain	\N
2784	\N	Salt River Valley	\N
2785	\N	VU (ISO Country Code)	ISO_COUNTRY
2786	\N	Republic of Vanuatu (Country)	COUNTRY
2787	\N	Vanuatu (State / Territory)	STATE
2788	\N	Southern Africa	\N
2789	\N	Kalahari	\N
2791	\N	Classic	\N
2792	\N	Post-Classic	\N
2793	\N	Oaxaca	\N
2794	\N	Iran	\N
2795	\N	Al Jahrah (State / Territory)	STATE
2796	\N	Chahar Mahall va Bakhtiari (State / Territory)	STATE
2797	\N	Esfahan (State / Territory)	STATE
2798	\N	Maysan (State / Territory)	STATE
2799	\N	Ilam (State / Territory)	STATE
2800	\N	IQ (ISO Country Code)	ISO_COUNTRY
2801	\N	Al Basrah (State / Territory)	STATE
2802	\N	Republic of Iraq (Country)	COUNTRY
2803	\N	Kohkiluyeh va buyer Ahmadi (State / Territory)	STATE
2804	\N	IR (ISO Country Code)	ISO_COUNTRY
2805	\N	Dhi Qar (State / Territory)	STATE
2806	\N	Lorestan (State / Territory)	STATE
2807	\N	Islamic Republic of Iran (Country)	COUNTRY
2808	\N	Khuzestan (State / Territory)	STATE
2809	\N	Dolores River Valley	\N
2810	\N	Southwestern Colorado	\N
2811	\N	IT (ISO Country Code)	ISO_COUNTRY
2812	\N	Italian Republic (Country)	COUNTRY
2813	\N	Lazio (State / Territory)	STATE
2814	\N	Not limited to any particular region	\N
2815	\N	New York State	\N
2816	\N	Western New York	\N
2817	\N	Northern Transversal Strip	\N
2818	\N	Maya highland-lowland transition	\N
2819	\N	Coban, Alta Verapaz	\N
2820	\N	Ixcan, El Quiche	\N
2821	\N	Southwestern Wisconsin	\N
2822	\N	Driftless Area	\N
2823	\N	Chesapeake	\N
2824	\N	Tidewater	\N
2825	\N	state of utah	\N
2826	\N	Hadarom (State / Territory)	STATE
2827	\N	IL (ISO Country Code)	ISO_COUNTRY
2828	\N	State of Israel (Country)	COUNTRY
2829	\N	Coronado National Forest	\N
2830	\N	Meadow Valley	\N
2831	\N	Cordes Junction	\N
2832	\N	Marble Canyon	\N
2833	\N	Palmetto Bend Reservoir	\N
2834	\N	Abiquiu Reservoir	\N
2835	\N	Navidad River	\N
2836	\N	Mustang Creek	\N
2837	\N	Rhone-Alpes (State / Territory)	STATE
2838	\N	Marsden-Saddleworth	\N
2839	\N	Tatton Mere	\N
2840	\N	Tatton Park	\N
2841	\N	Republic of South Africa (Country)	COUNTRY
2842	\N	Northern (State / Territory)	STATE
2843	\N	ZA (ISO Country Code)	ISO_COUNTRY
2844	\N	Lebanese Republic (Country)	COUNTRY
2845	\N	Dimashq (State / Territory)	STATE
2846	\N	An Nabatiyah (State / Territory)	STATE
2847	\N	Sayda (State / Territory)	STATE
2848	\N	Syrian Arab Republic (Country)	COUNTRY
2849	\N	Jabal Lubnan (State / Territory)	STATE
2850	\N	Al Qunaytirah (State / Territory)	STATE
2851	\N	LB (ISO Country Code)	ISO_COUNTRY
2852	\N	Dar'a (State / Territory)	STATE
2853	\N	Al Mafraq (State / Territory)	STATE
2854	\N	Hazafon (State / Territory)	STATE
2855	\N	West Bank (Country)	COUNTRY
2856	\N	Hefa (State / Territory)	STATE
2857	\N	Bayrut (State / Territory)	STATE
2858	\N	Al Biqa' (State / Territory)	STATE
2859	\N	SY (ISO Country Code)	ISO_COUNTRY
2860	\N	West Bank (State / Territory)	STATE
2861	\N	Irbid (State / Territory)	STATE
2862	\N	Llano Grande Mexico	\N
2863	\N	Jalisco Mexico	\N
2864	\N	Highlands Lake District Mexico	\N
2865	\N	Northeast Arkansas	\N
2866	\N	Kennewick	\N
2867	\N	Columbia River	\N
2868	\N	Pacific Northwest	\N
2869	\N	Sapelo Island	\N
2870	\N	San Juan Basin	\N
2871	\N	Machu Picchu, Peru	\N
2872	\N	Cusco (State / Territory)	STATE
2873	\N	Nyanza Province, Kenya	\N
2874	\N	Nyanza (State / Territory)	STATE
2875	\N	Rift Valley (State / Territory)	STATE
2876	\N	KE (ISO Country Code)	ISO_COUNTRY
2877	\N	Republic of Kenya (Country)	COUNTRY
2878	\N	Western (State / Territory)	STATE
2879	\N	Busoga (State / Territory)	STATE
2880	\N	Arusha (State / Territory)	STATE
2881	\N	United Republic of Tanzania (Country)	COUNTRY
2882	\N	TZ (ISO Country Code)	ISO_COUNTRY
2883	\N	central New York	\N
2884	\N	Finger Lakes	\N
2885	\N	Ithaca, New York	\N
2886	\N	Lake Cayuga	\N
2887	\N	Sydney NSW Australia	\N
2888	\N	Sydney, New South Wales, Australia	\N
2889	\N	London, England	\N
2890	\N	Stoke-on-Trent, England	\N
2891	\N	The Rocks, Sydney, NSW, Australia	\N
2892	\N	Sydney, NSW, Australia	\N
2893	\N	17 Test Street, Test City NSW	\N
2894	\N	Victoria (State / Territory)	STATE
2895	\N	Australia (Continent)	CONTINENT
2896	\N	Commonwealth of Australia (Country)	COUNTRY
2897	\N	AU (ISO Country Code)	ISO_COUNTRY
2898	\N	New South Wales (State / Territory)	STATE
2899	\N	Europe (Continent)	CONTINENT
2900	\N	Burslem, England	\N
2901	\N	The Rocks	\N
2902	\N	NSW	\N
2903	\N	Australia	\N
2904	\N	Sydney	\N
2905	\N	South Australia (State / Territory)	STATE
2906	\N	Western Australia (State / Territory)	STATE
2907	\N	Australian Capital Territory (State / Territory)	STATE
2908	\N	Northern Territory (State / Territory)	STATE
2909	\N	Queensland (State / Territory)	STATE
2910	\N	Tasmania (State / Territory)	STATE
2911	\N	Camperdown, Sydney, NSW, Australia	\N
2912	\N	Haymarket, Sydney, NSW, Australia	\N
2913	\N	Hobart 	\N
2914	\N	Tasmania	\N
2915	\N	Ballarat	\N
2916	\N	Manly	\N
2917	\N	Parramatta	\N
2918	\N	Kanahooka	\N
2919	\N	Illawarra (Region)	\N
2920	\N	Chippendale	\N
2921	\N	Castlereagh	\N
2922	\N	Sydney 	\N
2923	\N	Penrith Lakes	\N
2924	\N	London	\N
2925	\N	Lambeth	\N
2926	\N	England	\N
2927	\N	Melbourne	\N
2928	\N	Chicago	\N
2929	\N	CC (ISO Country Code)	ISO_COUNTRY
2930	\N	ER (ISO Country Code)	ISO_COUNTRY
2931	\N	MR (ISO Country Code)	ISO_COUNTRY
2932	\N	ID (ISO Country Code)	ISO_COUNTRY
2933	\N	Republic of Djibouti (Country)	COUNTRY
2934	\N	Territory of Christmas Island (Country)	COUNTRY
2935	\N	Argentine Republic (Country)	COUNTRY
2936	\N	BI (ISO Country Code)	ISO_COUNTRY
2937	\N	Kingdom of Cambodia (Country)	COUNTRY
2938	\N	Republic of the Philippines (Country)	COUNTRY
2939	\N	FI (ISO Country Code)	ISO_COUNTRY
2940	\N	DJ (ISO Country Code)	ISO_COUNTRY
2941	\N	VN (ISO Country Code)	ISO_COUNTRY
2942	\N	KI (ISO Country Code)	ISO_COUNTRY
2943	\N	CM (ISO Country Code)	ISO_COUNTRY
2944	\N	Kingdom of Spain (Country)	COUNTRY
2945	\N	Solomon Islands (Country)	COUNTRY
2946	\N	AM (ISO Country Code)	ISO_COUNTRY
2947	\N	Republic of Cape Verde (Country)	COUNTRY
2948	\N	Principality of Monaco (Country)	COUNTRY
2949	\N	UM (ISO Country Code)	ISO_COUNTRY
2950	\N	Territorial Collectivity of Mayotte (Country)	COUNTRY
2951	\N	Oriental Republic of Uruguay (Country)	COUNTRY
2952	\N	Republic of Latvia (Country)	COUNTRY
2953	\N	BG (ISO Country Code)	ISO_COUNTRY
2954	\N	CN (ISO Country Code)	ISO_COUNTRY
2955	\N	GN (ISO Country Code)	ISO_COUNTRY
2956	\N	Gibraltar (Country)	COUNTRY
2957	\N	YT (ISO Country Code)	ISO_COUNTRY
2958	\N	Kingdom of Nepal (Country)	COUNTRY
2959	\N	Republic of Ghana (Country)	COUNTRY
2960	\N	Republic of Austria (Country)	COUNTRY
2961	\N	BF (ISO Country Code)	ISO_COUNTRY
2962	\N	DE (ISO Country Code)	ISO_COUNTRY
2963	\N	FM (ISO Country Code)	ISO_COUNTRY
2964	\N	CV (ISO Country Code)	ISO_COUNTRY
2965	\N	Republic of Uzbekistan (Country)	COUNTRY
2966	\N	KP (ISO Country Code)	ISO_COUNTRY
2967	\N	Kingdom of Swaziland (Country)	COUNTRY
2968	\N	People's Republic of Bangladesh (Country)	COUNTRY
2969	\N	Republic of Serbia (Country)	COUNTRY
2970	\N	MW (ISO Country Code)	ISO_COUNTRY
2971	\N	Republic of Maldives (Country)	COUNTRY
2972	\N	GA (ISO Country Code)	ISO_COUNTRY
2973	\N	SB (ISO Country Code)	ISO_COUNTRY
2974	\N	GR (ISO Country Code)	ISO_COUNTRY
2975	\N	Republic of Bulgaria (Country)	COUNTRY
2976	\N	RE (ISO Country Code)	ISO_COUNTRY
2977	\N	Republic of Palau (Country)	COUNTRY
2978	\N	Gaza Strip (Country)	COUNTRY
2979	\N	CH (ISO Country Code)	ISO_COUNTRY
2980	\N	ET (ISO Country Code)	ISO_COUNTRY
2981	\N	NU (ISO Country Code)	ISO_COUNTRY
2982	\N	Republic of Rwanda (Country)	COUNTRY
2983	\N	MG (ISO Country Code)	ISO_COUNTRY
2984	\N	Sao Tome and Principe (Country)	COUNTRY
2985	\N	PL (ISO Country Code)	ISO_COUNTRY
2986	\N	KW (ISO Country Code)	ISO_COUNTRY
2987	\N	Kingdom of Morocco (Country)	COUNTRY
2988	\N	CF (ISO Country Code)	ISO_COUNTRY
2989	\N	Togolese Republic (Country)	COUNTRY
2990	\N	South America (Continent)	CONTINENT
2991	\N	Mongolia (Country)	COUNTRY
2992	\N	GM (ISO Country Code)	ISO_COUNTRY
2993	\N	TM (ISO Country Code)	ISO_COUNTRY
2994	\N	Republic of Hungary (Country)	COUNTRY
2995	\N	State of Eritrea (Country)	COUNTRY
2996	\N	Japan (Country)	COUNTRY
2997	\N	ML (ISO Country Code)	ISO_COUNTRY
2998	\N	ST (ISO Country Code)	ISO_COUNTRY
2999	\N	Czech Republic (Country)	COUNTRY
3000	\N	MP (ISO Country Code)	ISO_COUNTRY
3001	\N	Cook Islands (Country)	COUNTRY
3002	\N	Republic of Uganda (Country)	COUNTRY
3003	\N	Republic of Niger (Country)	COUNTRY
3004	\N	LK (ISO Country Code)	ISO_COUNTRY
3005	\N	KG (ISO Country Code)	ISO_COUNTRY
3006	\N	Democratic Socialist Republic of Sri Lan (Country)	COUNTRY
3007	\N	Arab Republic of Egypt (Country)	COUNTRY
3008	\N	Republic of Singapore (Country)	COUNTRY
3009	\N	Republic of the Congo (Country)	COUNTRY
3010	\N	State of Qatar (Country)	COUNTRY
3011	\N	GW (ISO Country Code)	ISO_COUNTRY
3012	\N	Commonwealth of the Northern Mariana Isl (Country)	COUNTRY
3013	\N	Republic of Mauritius (Country)	COUNTRY
3014	\N	BY (ISO Country Code)	ISO_COUNTRY
3015	\N	Malaysia (Country)	COUNTRY
3016	\N	Jarvis Island (Country)	COUNTRY
3017	\N	BA (ISO Country Code)	ISO_COUNTRY
3018	\N	Central African Republic (Country)	COUNTRY
3019	\N	Pitcairn, Henderson, Ducie, and Oeno Isl (Country)	COUNTRY
3020	\N	Kingdom of the Netherlands (Country)	COUNTRY
3021	\N	RW (ISO Country Code)	ISO_COUNTRY
3022	\N	Union of Myanmar (Country)	COUNTRY
3023	\N	HU (ISO Country Code)	ISO_COUNTRY
3024	\N	TJ (ISO Country Code)	ISO_COUNTRY
3025	\N	United Arab Emirates (Country)	COUNTRY
3026	\N	Islamic Republic of Mauritania (Country)	COUNTRY
3027	\N	DZ (ISO Country Code)	ISO_COUNTRY
3028	\N	TF (ISO Country Code)	ISO_COUNTRY
3029	\N	Republic of San Marino (Country)	COUNTRY
3030	\N	Jan Mayen (Country)	COUNTRY
3031	\N	Republic of Malawi (Country)	COUNTRY
3032	\N	NL (ISO Country Code)	ISO_COUNTRY
3033	\N	LY (ISO Country Code)	ISO_COUNTRY
3034	\N	CK (ISO Country Code)	ISO_COUNTRY
3035	\N	Federal Republic of Nigeria (Country)	COUNTRY
3036	\N	Republic of Paraguay (Country)	COUNTRY
3037	\N	Republic of Burundi (Country)	COUNTRY
3038	\N	SA (ISO Country Code)	ISO_COUNTRY
3039	\N	SJ (ISO Country Code)	ISO_COUNTRY
3040	\N	KZ (ISO Country Code)	ISO_COUNTRY
3041	\N	AZ (ISO Country Code)	ISO_COUNTRY
3042	\N	BJ (ISO Country Code)	ISO_COUNTRY
3043	\N	Falkland Islands (Islas Malvinas) (Country)	COUNTRY
3044	\N	Federal Democratic Republic of Ethiopia (Country)	COUNTRY
3045	\N	PY (ISO Country Code)	ISO_COUNTRY
3046	\N	Oceania (Continent)	CONTINENT
3047	\N	SK (ISO Country Code)	ISO_COUNTRY
3048	\N	Republic of Seychelles (Country)	COUNTRY
3049	\N	HR (ISO Country Code)	ISO_COUNTRY
3050	\N	Republic of Armenia (Country)	COUNTRY
3051	\N	UZ (ISO Country Code)	ISO_COUNTRY
3052	\N	AR (ISO Country Code)	ISO_COUNTRY
3053	\N	SI (ISO Country Code)	ISO_COUNTRY
3054	\N	LV (ISO Country Code)	ISO_COUNTRY
3055	\N	IN (ISO Country Code)	ISO_COUNTRY
3056	\N	LR (ISO Country Code)	ISO_COUNTRY
3057	\N	KM (ISO Country Code)	ISO_COUNTRY
3058	\N	Republic of Cote d'Ivoire (Country)	COUNTRY
3059	\N	Territory of French Polynesia (Country)	COUNTRY
3060	\N	NE (ISO Country Code)	ISO_COUNTRY
3061	\N	LI (ISO Country Code)	ISO_COUNTRY
3062	\N	LT (ISO Country Code)	ISO_COUNTRY
3063	\N	CX (ISO Country Code)	ISO_COUNTRY
3064	\N	Republic of Equatorial Guinea (Country)	COUNTRY
3065	\N	SC (ISO Country Code)	ISO_COUNTRY
3066	\N	Republic of Poland (Country)	COUNTRY
3067	\N	Principality of Andorra (Country)	COUNTRY
3068	\N	YE (ISO Country Code)	ISO_COUNTRY
3069	\N	SL (ISO Country Code)	ISO_COUNTRY
3070	\N	AO (ISO Country Code)	ISO_COUNTRY
3071	\N	Republic of Cyprus (Country)	COUNTRY
3072	\N	Republic of Albania (Country)	COUNTRY
3073	\N	GQ (ISO Country Code)	ISO_COUNTRY
3074	\N	Islamic Republic of Afghanistan (Country)	COUNTRY
3075	\N	ZW (ISO Country Code)	ISO_COUNTRY
3076	\N	People's Democratic Republic of Algeria (Country)	COUNTRY
3077	\N	SD (ISO Country Code)	ISO_COUNTRY
3078	\N	Republic of Zimbabwe (Country)	COUNTRY
3079	\N	Republic of Guinea (Country)	COUNTRY
3080	\N	Negara Brunei Darussalam (Country)	COUNTRY
3081	\N	PG (ISO Country Code)	ISO_COUNTRY
3082	\N	Republic of Madagascar (Country)	COUNTRY
3083	\N	Africa (Continent)	CONTINENT
3084	\N	Gabonese Republic (Country)	COUNTRY
3085	\N	Romania (Country)	COUNTRY
3086	\N	PN (ISO Country Code)	ISO_COUNTRY
3087	\N	RO (ISO Country Code)	ISO_COUNTRY
3088	\N	Republic of India (Country)	COUNTRY
3089	\N	Department of Guiana (Country)	COUNTRY
3090	\N	HM (ISO Country Code)	ISO_COUNTRY
3091	\N	MU (ISO Country Code)	ISO_COUNTRY
3092	\N	Territory of the French Southern and Ant (Country)	COUNTRY
3093	\N	Republic of Liberia (Country)	COUNTRY
3094	\N	State of Kuwait (Country)	COUNTRY
3095	\N	Republic of Belarus (Country)	COUNTRY
3096	\N	BN (ISO Country Code)	ISO_COUNTRY
3097	\N	Republic of Azerbaijan (Country)	COUNTRY
3098	\N	St. Helena (Country)	COUNTRY
3099	\N	CG (ISO Country Code)	ISO_COUNTRY
3100	\N	GS (ISO Country Code)	ISO_COUNTRY
3101	\N	Republic of Benin (Country)	COUNTRY
3102	\N	PF (ISO Country Code)	ISO_COUNTRY
3103	\N	Kyrgyz Republic (Country)	COUNTRY
3104	\N	Union of the Comoros (Country)	COUNTRY
3105	\N	Ukraine (Country)	COUNTRY
3106	\N	JP (ISO Country Code)	ISO_COUNTRY
3107	\N	Republic of Chile (Country)	COUNTRY
3108	\N	Georgia (Country)	COUNTRY
3109	\N	Democratic Republic of the Congo (Country)	COUNTRY
3110	\N	Democratic People's Republic of Korea (Country)	COUNTRY
3111	\N	QA (ISO Country Code)	ISO_COUNTRY
3112	\N	MK (ISO Country Code)	ISO_COUNTRY
3113	\N	Republic of Indonesia (Country)	COUNTRY
3114	\N	CD (ISO Country Code)	ISO_COUNTRY
3115	\N	Department of Reunion (Country)	COUNTRY
3116	\N	ES (ISO Country Code)	ISO_COUNTRY
3117	\N	Swiss Confederation (Country)	COUNTRY
3118	\N	Great Socialist People's Libyan Arab Jam (Country)	COUNTRY
3119	\N	MY (ISO Country Code)	ISO_COUNTRY
3120	\N	GI (ISO Country Code)	ISO_COUNTRY
3121	\N	Republic of Cameroon (Country)	COUNTRY
3122	\N	Kingdom of Thailand (Country)	COUNTRY
3123	\N	MN (ISO Country Code)	ISO_COUNTRY
3124	\N	Kingdom of Lesotho (Country)	COUNTRY
3125	\N	LU (ISO Country Code)	ISO_COUNTRY
3126	\N	RS (ISO Country Code)	ISO_COUNTRY
3127	\N	BH (ISO Country Code)	ISO_COUNTRY
3128	\N	MV (ISO Country Code)	ISO_COUNTRY
3129	\N	AL (ISO Country Code)	ISO_COUNTRY
3130	\N	Republic of Montenegro (Country)	COUNTRY
3131	\N	Juan De Nova Island (Country)	COUNTRY
3132	\N	CZ (ISO Country Code)	ISO_COUNTRY
3133	\N	GF (ISO Country Code)	ISO_COUNTRY
3134	\N	CI (ISO Country Code)	ISO_COUNTRY
3135	\N	OM (ISO Country Code)	ISO_COUNTRY
3136	\N	Bosnia and Herzegovina (Country)	COUNTRY
3137	\N	Republic of Lithuania (Country)	COUNTRY
3138	\N	TH (ISO Country Code)	ISO_COUNTRY
3139	\N	Republic of Chad (Country)	COUNTRY
3140	\N	KH (ISO Country Code)	ISO_COUNTRY
3141	\N	LS (ISO Country Code)	ISO_COUNTRY
3142	\N	Republic of Finland (Country)	COUNTRY
3143	\N	Kingdom of Saudi Arabia (Country)	COUNTRY
3144	\N	Lao People's Democratic Republic (Country)	COUNTRY
3145	\N	Independent State of Papua New Guinea (Country)	COUNTRY
3146	\N	UG (ISO Country Code)	ISO_COUNTRY
3147	\N	UA (ISO Country Code)	ISO_COUNTRY
3148	\N	TG (ISO Country Code)	ISO_COUNTRY
3149	\N	Kingdom of Bhutan (Country)	COUNTRY
3150	\N	MD (ISO Country Code)	ISO_COUNTRY
3151	\N	The Former Yugoslav Republic of Macedoni (Country)	COUNTRY
3152	\N	Republic of Kazakhstan (Country)	COUNTRY
3153	\N	Islamic Republic of Pakistan (Country)	COUNTRY
3154	\N	KR (ISO Country Code)	ISO_COUNTRY
3155	\N	Republic of Angola (Country)	COUNTRY
3156	\N	CL (ISO Country Code)	ISO_COUNTRY
3157	\N	Slovak Republic (Country)	COUNTRY
3158	\N	Somalia (Country)	COUNTRY
3159	\N	Republic of the Sudan (Country)	COUNTRY
3160	\N	TN (ISO Country Code)	ISO_COUNTRY
3161	\N	GU (ISO Country Code)	ISO_COUNTRY
3162	\N	Territory of Heard Island and McDonald I (Country)	COUNTRY
3163	\N	Federal Republic of Germany (Country)	COUNTRY
3164	\N	PS (ISO Country Code)	ISO_COUNTRY
3165	\N	Republic of Zambia (Country)	COUNTRY
3166	\N	South Georgia and the South Sandwich Is (Country)	COUNTRY
3167	\N	Republic of Guinea-Bissau (Country)	COUNTRY
3168	\N	British Indian Ocean Territory (Country)	COUNTRY
3169	\N	Niue (Country)	COUNTRY
3170	\N	Territory of Guam (Country)	COUNTRY
3171	\N	Bouvet Island (Country)	COUNTRY
3172	\N	TL (ISO Country Code)	ISO_COUNTRY
3173	\N	Republic of Mali (Country)	COUNTRY
3174	\N	SM (ISO Country Code)	ISO_COUNTRY
3175	\N	LA (ISO Country Code)	ISO_COUNTRY
3176	\N	MT (ISO Country Code)	ISO_COUNTRY
3177	\N	UY (ISO Country Code)	ISO_COUNTRY
3178	\N	Socialist Republic of Vietnam (Country)	COUNTRY
3179	\N	Republic of Malta (Country)	COUNTRY
3180	\N	EH (ISO Country Code)	ISO_COUNTRY
3181	\N	MA (ISO Country Code)	ISO_COUNTRY
3182	\N	Republic of Moldova (Country)	COUNTRY
3183	\N	Territory of Cocos (Keeling) Islands (Country)	COUNTRY
3184	\N	SN (ISO Country Code)	ISO_COUNTRY
3185	\N	Western Sahara (Country)	COUNTRY
3186	\N	Asia (Continent)	CONTINENT
3187	\N	Republic of Korea (Country)	COUNTRY
3188	\N	Hellenic Republic (Country)	COUNTRY
3189	\N	Bailiwick of Jersey (Country)	COUNTRY
3190	\N	PT (ISO Country Code)	ISO_COUNTRY
3191	\N	Republic of Estonia (Country)	COUNTRY
3192	\N	GH (ISO Country Code)	ISO_COUNTRY
3193	\N	SH (ISO Country Code)	ISO_COUNTRY
3194	\N	GE (ISO Country Code)	ISO_COUNTRY
3195	\N	Democratic Republic of Timor-Leste (Country)	COUNTRY
3196	\N	PW (ISO Country Code)	ISO_COUNTRY
3197	\N	ME (ISO Country Code)	ISO_COUNTRY
3198	\N	EG (ISO Country Code)	ISO_COUNTRY
3199	\N	Republic of Sierra Leone (Country)	COUNTRY
3200	\N	ZM (ISO Country Code)	ISO_COUNTRY
3201	\N	Federated States of Micronesia (Country)	COUNTRY
3202	\N	BD (ISO Country Code)	ISO_COUNTRY
3203	\N	FK (ISO Country Code)	ISO_COUNTRY
3204	\N	Principality of Liechtenstein (Country)	COUNTRY
3205	\N	NG (ISO Country Code)	ISO_COUNTRY
3206	\N	EE (ISO Country Code)	ISO_COUNTRY
3207	\N	PK (ISO Country Code)	ISO_COUNTRY
3208	\N	Republic of Senegal (Country)	COUNTRY
3209	\N	Tunisian Republic (Country)	COUNTRY
3210	\N	Portuguese Republic (Country)	COUNTRY
3211	\N	Republic of Yemen (Country)	COUNTRY
3212	\N	NP (ISO Country Code)	ISO_COUNTRY
3213	\N	Turkmenistan (Country)	COUNTRY
3214	\N	IO (ISO Country Code)	ISO_COUNTRY
3215	\N	MZ (ISO Country Code)	ISO_COUNTRY
3216	\N	CY (ISO Country Code)	ISO_COUNTRY
3217	\N	Burkina Faso (Country)	COUNTRY
3218	\N	AD (ISO Country Code)	ISO_COUNTRY
3219	\N	The Holy See (State of the Vatican City) (Country)	COUNTRY
3220	\N	Kingdom of Denmark (Country)	COUNTRY
3221	\N	Grand Duchy of Luxembourg (Country)	COUNTRY
3222	\N	SO (ISO Country Code)	ISO_COUNTRY
3223	\N	BT (ISO Country Code)	ISO_COUNTRY
3224	\N	AF (ISO Country Code)	ISO_COUNTRY
3225	\N	AE (ISO Country Code)	ISO_COUNTRY
3226	\N	Republic of Kiribati (Country)	COUNTRY
3227	\N	SG (ISO Country Code)	ISO_COUNTRY
3228	\N	MC (ISO Country Code)	ISO_COUNTRY
3229	\N	Johnston Atoll (Country)	COUNTRY
3230	\N	PH (ISO Country Code)	ISO_COUNTRY
3231	\N	Republic of Mozambique (Country)	COUNTRY
3232	\N	SZ (ISO Country Code)	ISO_COUNTRY
3233	\N	People's Republic of China (Country)	COUNTRY
3234	\N	Kingdom of Bahrain (Country)	COUNTRY
3235	\N	AT (ISO Country Code)	ISO_COUNTRY
3236	\N	BV (ISO Country Code)	ISO_COUNTRY
3237	\N	North America (Continent)	CONTINENT
3238	\N	Sultanate of Oman (Country)	COUNTRY
3239	\N	Republic of Slovenia (Country)	COUNTRY
3240	\N	Republic of Tajikistan (Country)	COUNTRY
3241	\N	Republic of Croatia (Country)	COUNTRY
3242	\N	DK (ISO Country Code)	ISO_COUNTRY
3243	\N	Glorioso Islands (Country)	COUNTRY
3244	\N	MM (ISO Country Code)	ISO_COUNTRY
3245	\N	Republic of The Gambia (Country)	COUNTRY
3246	\N	TD (ISO Country Code)	ISO_COUNTRY
3247	\N	Viewbank	\N
3248	\N	Banyule City	\N
3249	\N	Little Lon Precinct	\N
\.


--
-- TOC entry 2174 (class 0 OID 79106)
-- Dependencies: 190
-- Data for Name: investigation_type; Type: TABLE DATA; Schema: public; Owner: tdar
--

COPY investigation_type (id, definition, label) FROM stdin;
12	A project which assesses the potential for archaeological heritage. Associated documents may include an Archaeological Assessment, Archaeological Management Plan or Archaeological Zoning Plan. 	Archaeological assessment
13	A project which outlines a comprehensive proposal for the excavation of an archaeological site, including major research questions. In some states, a Research Design is required prior to the issue of an excavation permit.	Archaeological research design
14	A project which involves systematic, physical investigation and recording of subsurface features on an archaeological site, including test-trenching and open-area excavation. 	Archaeological excavation
15	A project which involves systematic, physical investigation and recording of aboveground features of an archaeological site.	Archaeological survey
16	A project which identifies and catalogues artefacts recovered from an archaeological site. 	Artefact catalogue
17	A project which undertakes numerical comparison of artefact catalogue data.	Assemblage analysis
18	A project which undertakes chemical or microscopic analysis of pollen samples recovered from archaeological sites.	Palynological analysis
19	A project which carries out any other kind of investigation or assessment of archaeological sites or relics, or associated documentation, not identified above.	Other contract research
20	A project completed in fulfilment of a Doctorate of Philosophy.	PhD Research
21	A project completed in fulfilment of an honours or masters degree in archaeology or associated fields.	Honours/Masters Research
22	A project completed by a postgraduate fellow or associate undertaking publicly funded research.	Postgraduate Research
23	A project undertaking publicly funded research administered by a university which is not identified above.	Other Academic Research
24	A project undertaken to guide or support the management of an archaeological collection held by a museum or associated repository.	Collections Management
25	A project undertaken to enhance research about an archaeological collection held by a museum or associated repository.	Collections Research
26	A project undertaking privately funded research, not administered by a university or undertaken in compliance with legislation.	Other Research
\.


--
-- TOC entry 2175 (class 0 OID 79120)
-- Dependencies: 194
-- Data for Name: material_keyword; Type: TABLE DATA; Schema: public; Owner: tdar
--

COPY material_keyword (id, definition, label) FROM stdin;
16	Refined earthenware, stoneware, porcelain and porcellaneous and other domestic-grade ceramics. Typically excludes ceramic building materials (brick, pipe etc), clay pipes, ceramic dolls, marbles and other ‘miscellaneous’ items.	Ceramic
17	Clear and coloured glass from bottles, tablewares, lamps, windows and other domestic-grade glass. Typically excludes glass buttons, beads and other ‘miscellaneous’ items.	Glass
18	Robust remains of alloy metal domestic objects including nails, furnishing pins & tacks, cans and unidentified fragments. Typically excludes small fragile items such as jewellery elements. 	Metal
20	Unworked bone, shell, stone, mineral or other ‘ecofact’	Organic
21	Large, coarse fragments of building materials or architectural elements including brick, sewerage pipes, roof tiles, door jambs, plaster & mortar. Typically excludes nails and window glass. 	Building Materials
19	Fragile or ‘small finds’ (e.g. buttons, beads, jewellery elements, pins & needles) from a range of miscellaneous material types including worked bone and shell, worked stone, leather, synthetics, and composite items. Typically includes clay pipes, children’s toys and coins. Typically excludes robust or heavy items. 	Miscellaneous
22	Any other material class not listed above.	Other
\.


--
-- TOC entry 2176 (class 0 OID 79304)
-- Dependencies: 238
-- Data for Name: site_type_keyword; Type: TABLE DATA; Schema: public; Owner: tdar
--

COPY site_type_keyword (id, definition, label, approved, index, selectable, parent_id) FROM stdin;
403	\N	Camp	t	1.2.3	t	399
404	\N	Industrial	t	2	t	\N
429	\N	Recreational	f	\N	f	\N
409	\N	Mining	t	2.2	t	404
395	\N	Domestic	t	1	t	\N
396	\N	Urban	t	1.1	t	395
410	\N	Pastoral	t	2.3	t	404
411	\N	Infrastructure	t	2.4	t	404
412	\N	Commercial	t	3	t	\N
413	\N	Shop	t	3.1	t	412
414	\N	Office	t	3.2	t	412
415	\N	Warehouse	t	3.3	t	412
416	\N	Shipwreck	t	3.4	t	412
399	\N	Rural	t	1.2	t	395
400	\N	Homestead	t	1.2.1	t	399
401	\N	Staff Quarters	t	1.2.2	t	399
397	\N	Cottage	t	1.1.2	t	396
398	\N	House	t	1.1.3	t	396
402	\N	Terrace	t	1.1.1	t	396
422	\N	Military	t	5	t	\N
423	\N	Defence	t	5.1	t	422
424	\N	Accomodation	t	5.2	t	422
425	\N	Ceremonial	t	6	t	\N
426	\N	Church	t	6.1	t	425
427	\N	Burial Ground	t	6.2	t	425
417	\N	Institution	t	4	t	\N
418	\N	Hospital	t	4.1	t	417
419	\N	Asylum	t	4.2	t	417
420	\N	Gaol	t	4.3	t	417
421	\N	Municipal	t	4.4	t	417
405	\N	Manufacturing	t	2.1	t	404
406	\N	Metal Craft trades	t	2.1.1	t	405
407	\N	Pottery	t	2.1.2	t	405
408	\N	Food	t	2.1.3	t	405
\.


--
-- TOC entry 2177 (class 0 OID 79328)
-- Dependencies: 244
-- Data for Name: temporal_keyword; Type: TABLE DATA; Schema: public; Owner: tdar
--

COPY temporal_keyword (id, definition, label) FROM stdin;
237	\N	late-18th century
238	\N	19th century
239	\N	20th century
240	\N	18th century
241	\N	Victorian era
242	\N	early-20th century 
243	\N	Convict era
\.

